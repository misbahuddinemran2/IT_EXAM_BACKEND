package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.dto.*;
import com.examplatform.modules.exam.entity.ExamSession;
import com.examplatform.modules.exam.repository.ExamSessionRepository;
import com.examplatform.modules.user.entity.User;
import com.examplatform.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PracticeExamService {

    private final ExamSessionRepository examSessionRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public ExamSession startFreeExam(String userId) {
        String resolvedUserId = (userId == null || userId.isBlank())
                ? UUID.randomUUID().toString()
                : userId;

        if (!userRepository.existsById(resolvedUserId)) {
            User guestUser = User.builder()
                    .id(resolvedUserId)
                    .fullName("Guest User")
                    .build();
            userRepository.save(guestUser);
        }

        Integer availableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM questions WHERE status = 'APPROVED'", Integer.class);
        int totalQuestions = Math.min(15, availableCount != null ? availableCount : 0);

        ExamSession session = ExamSession.builder()
                .id(UUID.randomUUID().toString())
                .userId(resolvedUserId)
                .sessionType(ExamSession.SessionType.PRACTICE)
                .status(ExamSession.Status.IN_PROGRESS)
                .totalQuestions(totalQuestions)
                .attemptedCount(0)
                .correctCount(0)
                .wrongCount(0)
                .skipCount(0)
                .score(0)
                .percentage(0)
                .timeSpentSec(0)
                .isPassed(false)
                .build();

        return examSessionRepository.save(session);
    }

    public QuestionResponse getNextQuestion(String sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getAttemptedCount() >= session.getTotalQuestions()) {
            return null;
        }

        int attempted = session.getAttemptedCount();

        int difficultyLevel;
        if (attempted < 3) {
            difficultyLevel = 1;
        } else if (attempted < 8) {
            difficultyLevel = 2;
        } else {
            difficultyLevel = 3;
        }

        String cognitiveLevel;
        if (attempted < 4) {
            cognitiveLevel = "REMEMBER";
        } else if (attempted < 8) {
            cognitiveLevel = "UNDERSTAND";
        } else if (attempted < 12) {
            cognitiveLevel = "APPLY";
        } else {
            cognitiveLevel = "ANALYZE";
        }

        List<Map<String, Object>> topics = jdbcTemplate.queryForList(
                "SELECT DISTINCT q.topic_id FROM questions q " +
                        "WHERE q.status = 'APPROVED' AND q.topic_id IS NOT NULL " +
                        "ORDER BY RAND()"
        );
        String selectedTopicId = null;
        if (!topics.isEmpty()) {
            int topicIndex = (attempted / 3) % topics.size();
            selectedTopicId = (String) topics.get(topicIndex).get("topic_id");
        }

        List<Map<String, Object>> questions = new ArrayList<>();

        if (selectedTopicId != null) {
            questions = jdbcTemplate.queryForList(
                    "SELECT q.id, q.question_text, q.question_text_bn, q.question_type, q.estimated_time_sec " +
                            "FROM questions q " +
                            "WHERE q.status = 'APPROVED' " +
                            "AND q.difficulty_level = ? " +
                            "AND q.cognitive_level = ? " +
                            "AND q.topic_id = ? " +
                            "AND q.id NOT IN (" +
                            "  SELECT qa.question_id FROM user_question_attempts qa WHERE qa.session_id = ?" +
                            ") ORDER BY RAND() LIMIT 1",
                    difficultyLevel, cognitiveLevel, selectedTopicId, sessionId
            );
        }

        if (questions.isEmpty()) {
            questions = jdbcTemplate.queryForList(
                    "SELECT q.id, q.question_text, q.question_text_bn, q.question_type, q.estimated_time_sec " +
                            "FROM questions q " +
                            "WHERE q.status = 'APPROVED' " +
                            "AND q.difficulty_level = ? " +
                            "AND q.cognitive_level = ? " +
                            "AND q.id NOT IN (" +
                            "  SELECT qa.question_id FROM user_question_attempts qa WHERE qa.session_id = ?" +
                            ") ORDER BY RAND() LIMIT 1",
                    difficultyLevel, cognitiveLevel, sessionId
            );
        }

        if (questions.isEmpty()) {
            questions = jdbcTemplate.queryForList(
                    "SELECT q.id, q.question_text, q.question_text_bn, q.question_type, q.estimated_time_sec " +
                            "FROM questions q " +
                            "WHERE q.status = 'APPROVED' " +
                            "AND q.difficulty_level = ? " +
                            "AND q.id NOT IN (" +
                            "  SELECT qa.question_id FROM user_question_attempts qa WHERE qa.session_id = ?" +
                            ") ORDER BY RAND() LIMIT 1",
                    difficultyLevel, sessionId
            );
        }

        if (questions.isEmpty()) {
            questions = jdbcTemplate.queryForList(
                    "SELECT q.id, q.question_text, q.question_text_bn, q.question_type, q.estimated_time_sec " +
                            "FROM questions q " +
                            "WHERE q.status = 'APPROVED' " +
                            "AND q.id NOT IN (" +
                            "  SELECT qa.question_id FROM user_question_attempts qa WHERE qa.session_id = ?" +
                            ") ORDER BY RAND() LIMIT 1",
                    sessionId
            );
        }

        if (questions.isEmpty()) {
            return null;
        }

        Map<String, Object> question = questions.get(0);
        String questionId = (String) question.get("id");

        List<Map<String, Object>> optionsData = jdbcTemplate.queryForList(
                "SELECT id, option_key, option_text, option_text_bn, explanation, explanation_bn " +
                        "FROM options WHERE question_id = ? ORDER BY order_index",
                questionId
        );

        List<QuestionResponse.OptionResponse> options = optionsData.stream()
                .map(opt -> QuestionResponse.OptionResponse.builder()
                        .optionId((String) opt.get("id"))
                        .optionKey((String) opt.get("option_key"))
                        .optionText((String) opt.get("option_text"))
                        .optionTextBn((String) opt.get("option_text_bn"))
                        .build())
                .collect(Collectors.toList());

        return QuestionResponse.builder()
                .questionId(questionId)
                .questionText((String) question.get("question_text"))
                .questionTextBn((String) question.get("question_text_bn"))
                .questionType((String) question.get("question_type"))
                .estimatedTimeSec(question.get("estimated_time_sec") != null ?
                        ((Number) question.get("estimated_time_sec")).intValue() : 60)
                .questionNumber(session.getAttemptedCount() + 1)
                .totalQuestions(session.getTotalQuestions())
                .options(options)
                .build();
    }

    public AnswerResultResponse submitAnswer(String sessionId, SubmitAnswerRequest request) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        boolean isCorrect = false;
        String correctOptionId = "";
        String explanation = "";
        String explanationBn = "";

        if (!request.isSkipped() && request.getSelectedOptionId() != null) {
            List<Map<String, Object>> correctOption = jdbcTemplate.queryForList(
                    "SELECT id, explanation, explanation_bn FROM options " +
                            "WHERE question_id = ? AND is_correct = 1",
                    request.getQuestionId()
            );

            if (!correctOption.isEmpty()) {
                correctOptionId = (String) correctOption.get(0).get("id");
                explanation = (String) correctOption.get(0).get("explanation");
                explanationBn = (String) correctOption.get(0).get("explanation_bn");
                isCorrect = correctOptionId.equals(request.getSelectedOptionId());
            }

            jdbcTemplate.update(
                    "INSERT INTO user_question_attempts " +
                            "(id, session_id, question_id, selected_option_id, is_correct, is_skipped, time_spent_sec, answered_at) " +
                            "VALUES (UUID(), ?, ?, ?, ?, 0, ?, NOW())",
                    sessionId,
                    request.getQuestionId(),
                    request.getSelectedOptionId(),
                    isCorrect ? 1 : 0,
                    request.getTimeSpentSec()
            );

            session.setAttemptedCount(session.getAttemptedCount() + 1);
            session.setTimeSpentSec(session.getTimeSpentSec() + request.getTimeSpentSec());
            if (isCorrect) {
                session.setCorrectCount(session.getCorrectCount() + 1);
            } else {
                session.setWrongCount(session.getWrongCount() + 1);
            }

        } else {
            jdbcTemplate.update(
                    "INSERT INTO user_question_attempts " +
                            "(id, session_id, question_id, is_correct, is_skipped, time_spent_sec, answered_at) " +
                            "VALUES (UUID(), ?, ?, 0, 1, ?, NOW())",
                    sessionId,
                    request.getQuestionId(),
                    request.getTimeSpentSec()
            );

            session.setAttemptedCount(session.getAttemptedCount() + 1);
            session.setSkipCount(session.getSkipCount() + 1);
        }

        examSessionRepository.save(session);

        int remaining = session.getTotalQuestions() - session.getAttemptedCount();

        return AnswerResultResponse.builder()
                .isCorrect(isCorrect)
                .correctOptionId(correctOptionId)
                .explanation(explanation)
                .explanationBn(explanationBn)
                .attemptedCount(session.getAttemptedCount())
                .remainingCount(remaining)
                .build();
    }

    public ExamSession getSessionProgress(String sessionId) {
        return examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    public ExamResultResponse finishExam(String sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        double score = session.getCorrectCount() * 1.0 - session.getWrongCount() * 0.5;
        double maxScore = session.getTotalQuestions() * 1.0;
        double percentage = maxScore > 0 ? (score / maxScore) * 100 : 0;
        if (percentage < 0) percentage = 0;

        session.setStatus(ExamSession.Status.COMPLETED);
        session.setPercentage(percentage);
        session.setScore(score);
        session.setPassed(percentage >= 40);
        examSessionRepository.save(session);

        List<Map<String, Object>> attempts = jdbcTemplate.queryForList(
                "SELECT qa.question_id, qa.selected_option_id, qa.is_correct, qa.is_skipped, " +
                        "q.question_text, " +
                        "(SELECT option_text FROM options WHERE id = qa.selected_option_id LIMIT 1) as selected_option_text, " +
                        "(SELECT id FROM options WHERE question_id = q.id AND is_correct = 1 LIMIT 1) as correct_option_id, " +
                        "(SELECT option_text FROM options WHERE question_id = q.id AND is_correct = 1 LIMIT 1) as correct_option_text, " +
                        "(SELECT explanation FROM options WHERE question_id = q.id AND is_correct = 1 LIMIT 1) as explanation " +
                        "FROM user_question_attempts qa " +
                        "JOIN questions q ON qa.question_id = q.id " +
                        "WHERE qa.session_id = ? ORDER BY qa.answered_at",
                sessionId
        );

        List<ExamResultResponse.QuestionReviewResponse> reviews = attempts.stream()
                .map(a -> ExamResultResponse.QuestionReviewResponse.builder()
                        .questionId((String) a.get("question_id"))
                        .questionText((String) a.get("question_text"))
                        .selectedOptionId((String) a.get("selected_option_id"))
                        .selectedOptionText((String) a.get("selected_option_text"))
                        .correctOptionId((String) a.get("correct_option_id"))
                        .correctOptionText((String) a.get("correct_option_text"))
                        .Correct(Boolean.TRUE.equals(a.get("is_correct")))
                        .Skipped(Boolean.TRUE.equals(a.get("is_skipped")))
                        .explanation((String) a.get("explanation"))
                        .build())
                .collect(Collectors.toList());

        return ExamResultResponse.builder()
                .sessionId(sessionId)
                .sessionType(session.getSessionType().name())
                .totalQuestions(session.getTotalQuestions())
                .attemptedCount(session.getAttemptedCount())
                .correctCount(session.getCorrectCount())
                .wrongCount(session.getWrongCount())
                .skipCount(session.getSkipCount())
                .score(score)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .timeSpentSec(session.getTimeSpentSec())
                .isPassed(session.isPassed())
                .questionReviews(reviews)
                .build();
    }
}
