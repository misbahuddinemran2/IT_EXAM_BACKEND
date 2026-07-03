package com.examplatform.modules.liveexam.service;
import com.examplatform.modules.exam.entity.Exam;
import com.examplatform.modules.exam.entity.ExamQuestion;
import com.examplatform.modules.exam.entity.ExamSubjectConfig;
import com.examplatform.modules.exam.entity.ExamTopicConfig;
import com.examplatform.modules.exam.repository.ExamAttemptHistoryRepository;
import com.examplatform.modules.exam.repository.ExamQuestionRepository;
import com.examplatform.modules.exam.repository.ExamRepository;
import com.examplatform.modules.exam.repository.ExamSubjectConfigRepository;
import com.examplatform.modules.exam.repository.ExamTopicConfigRepository;
import com.examplatform.modules.exam.entity.ExamAttemptHistory;
import com.examplatform.modules.liveexam.dto.*;
import com.examplatform.modules.liveexam.entity.LiveExamSession;
import com.examplatform.modules.liveexam.repository.LiveExamSessionRepository;
import com.examplatform.modules.question.entity.Option;
import com.examplatform.modules.question.entity.Question;
import com.examplatform.modules.question.repository.OptionRepository;
import com.examplatform.modules.question.repository.QuestionRepository;
import com.examplatform.modules.taxonomy.repository.SubjectRepository;
import com.examplatform.modules.taxonomy.repository.ChapterRepository;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveExamService {

    private static final int GRACE_PERIOD_MINUTES = 5;
    private static final ZoneId BD_ZONE = ZoneId.of("Asia/Dhaka");

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final LiveExamSessionRepository liveSessionRepository;
    private final ExamAttemptHistoryRepository attemptHistoryRepository;
    private final ExamSubjectConfigRepository subjectConfigRepository;
    private final ExamTopicConfigRepository topicConfigRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;

    // ============================================
    // 1. VISIBILITY — আজকের published live exams
    // ============================================
    @Transactional(readOnly = true)
     public List<LiveExamSummaryResponse> getTodaysLiveExams(String userLevel, String userId) {
        LocalDate today = LocalDate.now(BD_ZONE);
        List<Exam> exams = examRepository.findByPublishStatusAndExamDate(
                Exam.PublishStatus.PUBLISHED, today);

        return exams.stream()
                .filter(e -> isVisibleToUser(e, userLevel))
                .map(e -> buildLiveExamSummary(e, userId))
                .collect(Collectors.toList());
    }

    private LiveExamSummaryResponse buildLiveExamSummary(Exam exam, String userId) { 
        List<ExamSubjectConfig> subjectConfigs = subjectConfigRepository.findByExamId(exam.getId());
        List<ExamTopicConfig> topicConfigs = topicConfigRepository.findByExamId(exam.getId());

        Set<String> subjectNames = new LinkedHashSet<>();
        Set<String> chapterNames = new LinkedHashSet<>();
        Set<String> topicNames = new LinkedHashSet<>();

        for (ExamSubjectConfig sc : subjectConfigs) {
            subjectRepository.findById(sc.getSubjectId())
                    .ifPresent(s -> subjectNames.add(s.getName()));
        }

        for (ExamTopicConfig tc : topicConfigs) {
            if (tc.getSubjectId() != null) {
                subjectRepository.findById(tc.getSubjectId())
                        .ifPresent(s -> subjectNames.add(s.getName()));
            }
            if (tc.getChapterId() != null) {
                chapterRepository.findById(tc.getChapterId())
                        .ifPresent(c -> chapterNames.add(c.getName()));
            }
            if (tc.getTopicId() != null) {
                topicRepository.findById(tc.getTopicId())
                        .ifPresent(t -> topicNames.add(t.getName()));
            }
        }

        return LiveExamSummaryResponse.builder()
                .id(exam.getId())
                .name(exam.getName())
                .examCode(exam.getExamCode())
                .examType(exam.getExamType().name())
                .description(exam.getDescription())
                .subjectNames(new ArrayList<>(subjectNames))
                .chapterNames(new ArrayList<>(chapterNames))
                .topicNames(new ArrayList<>(topicNames))
                .totalQuestions(exam.getTotalQuestions())
                .totalMarks(exam.getTotalMarks())
                .passMarks(exam.getPassMarks())
                .durationMinutes(exam.getDurationMinutes())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .targetLevels(exam.getTargetLevels())
                .isPremiumOnly(exam.isPremiumOnly())
                .attemptStatus(liveSessionRepository.findByExamIdAndUserId(exam.getId(), userId)
                        .map(s -> s.getStatus().name())
                        .orElse("NOT_STARTED"))
                .build();
 
    }

    private boolean isVisibleToUser(Exam exam, String userLevel) {
        List<String> levels = exam.getTargetLevels();
        if (levels == null || levels.isEmpty() || levels.contains("ALL")) return true;
        if (userLevel == null) return false;
        return levels.contains(userLevel);
    }

    // ============================================
    // 2. START EXAM
    // ============================================
    @Transactional
    public LiveExamStartResponse startExam(String examId, String userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        if (exam.getPublishStatus() != Exam.PublishStatus.PUBLISHED) {
            throw new RuntimeException("This exam is not published.");
        }

        LocalDate today = LocalDate.now(BD_ZONE);
        if (!exam.getExamDate().equals(today)) {
            throw new RuntimeException("This exam is not scheduled for today.");
        }

        LocalDateTime now = LocalDateTime.now(BD_ZONE);
        LocalDateTime windowEnd = LocalDateTime.of(today, LocalTime.of(23, 59, 59));
        if (now.isAfter(windowEnd)) {
            throw new RuntimeException("Exam window for today has closed.");
        }

        Optional<LiveExamSession> existing =
                liveSessionRepository.findByExamIdAndUserId(examId, userId);
        if (existing.isPresent()) {
            return resumeInternal(existing.get(), exam);
        }

        String sessionId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = now.plusMinutes(exam.getDurationMinutes());

        LiveExamSession session = LiveExamSession.builder()
                .id(sessionId)
                .examId(examId)
                .userId(userId)
                .status(LiveExamSession.Status.IN_PROGRESS)
                .startedAt(now)
                .expiresAt(expiresAt)
                .lastSeenAt(now)
                .answers(new HashMap<>())
                .markedForReview(new ArrayList<>())
                .totalMarks(exam.getTotalMarks())
                .build();

        try {
            liveSessionRepository.save(session);
        } catch (org.springframework.dao.DataIntegrityViolationException dup) {
            LiveExamSession existingRace = liveSessionRepository
                    .findByExamIdAndUserId(examId, userId)
                    .orElseThrow(() -> new RuntimeException("Could not start or resume exam."));
            return resumeInternal(existingRace, exam);
        }

        return buildStartResponse(session, exam);
    }

    // ============================================
    // 3. RESUME
    // ============================================
    @Transactional
    public LiveExamStartResponse resumeExam(String examId, String userId) {
        LiveExamSession session = liveSessionRepository.findByExamIdAndUserId(examId, userId)
                .orElseThrow(() -> new RuntimeException("No active session found. Please start the exam."));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        return resumeInternal(session, exam);
    }

    private LiveExamStartResponse resumeInternal(LiveExamSession session, Exam exam) {
        LocalDateTime now = LocalDateTime.now(BD_ZONE);

        if (session.getStatus() == LiveExamSession.Status.SUBMITTED
                || session.getStatus() == LiveExamSession.Status.AUTO_SUBMITTED) {
            throw new RuntimeException("This exam attempt is already finished.");
        }

        if (now.isAfter(session.getExpiresAt())) {
            autoSubmit(session, exam);
            throw new RuntimeException("Time is up. Your exam has been auto-submitted.");
        }

        if (session.getStatus() == LiveExamSession.Status.DISCONNECTED) {
            long minutesSinceDisconnect = ChronoUnit.MINUTES.between(session.getDisconnectedAt(), now);
            if (minutesSinceDisconnect > GRACE_PERIOD_MINUTES) {
                autoSubmit(session, exam);
                throw new RuntimeException("Grace period expired. Your exam has been auto-submitted.");
            }
            session.setStatus(LiveExamSession.Status.IN_PROGRESS);
            session.setDisconnectedAt(null);
        }

        session.setLastSeenAt(now);
        liveSessionRepository.save(session);

        return buildStartResponse(session, exam);
    }

    // ============================================
    // 4. HEARTBEAT
    // ============================================
    @Transactional
    public void heartbeat(String sessionId, String userId) {
        LiveExamSession session = getOwnedSession(sessionId, userId);
        if (session.getStatus() == LiveExamSession.Status.IN_PROGRESS
                || session.getStatus() == LiveExamSession.Status.DISCONNECTED) {
            session.setLastSeenAt(LocalDateTime.now(BD_ZONE));
            if (session.getStatus() == LiveExamSession.Status.DISCONNECTED) {
                session.setStatus(LiveExamSession.Status.IN_PROGRESS);
                session.setDisconnectedAt(null);
            }
            liveSessionRepository.save(session);
        }
    }

    @Transactional
    public void markDisconnected(String sessionId, String userId) {
        LiveExamSession session = getOwnedSession(sessionId, userId);
        if (session.getStatus() == LiveExamSession.Status.IN_PROGRESS) {
            session.setStatus(LiveExamSession.Status.DISCONNECTED);
            session.setDisconnectedAt(LocalDateTime.now(BD_ZONE));
            liveSessionRepository.save(session);
        }
    }

    // ============================================
    // 5. SUBMIT ANSWER
    // ============================================
    @Transactional
    public void submitAnswer(String sessionId, String userId, SubmitLiveAnswerRequest req) {
        LiveExamSession session = getOwnedSession(sessionId, userId);
        ensureActive(session);

        Map<String, String> answers = new HashMap<>(session.getAnswers());
        if (req.getSelectedOptionId() == null) {
            answers.remove(req.getQuestionId());
        } else {
            answers.put(req.getQuestionId(), req.getSelectedOptionId());
        }
        session.setAnswers(answers);

        if (req.getMarkForReview() != null) {
            List<String> marked = new ArrayList<>(session.getMarkedForReview());
            if (req.getMarkForReview()) {
                if (!marked.contains(req.getQuestionId())) marked.add(req.getQuestionId());
            } else {
                marked.remove(req.getQuestionId());
            }
            session.setMarkedForReview(marked);
        }

        session.setLastSeenAt(LocalDateTime.now(BD_ZONE));
        liveSessionRepository.save(session);
    }

    // ============================================
    // 6. FINISH
    // ============================================
    @Transactional
    public void finishExam(String sessionId, String userId) {
        LiveExamSession session = getOwnedSession(sessionId, userId);
        if (session.getStatus() == LiveExamSession.Status.SUBMITTED
                || session.getStatus() == LiveExamSession.Status.AUTO_SUBMITTED) {
            return;
        }
        Exam exam = examRepository.findById(session.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        gradeAndClose(session, exam, LiveExamSession.Status.SUBMITTED);
    }

    // ============================================
    // 7. AUTO-SUBMIT
    // ============================================
    @Transactional
    public void autoSubmit(LiveExamSession session, Exam exam) {
        if (session.getStatus() == LiveExamSession.Status.SUBMITTED
                || session.getStatus() == LiveExamSession.Status.AUTO_SUBMITTED) {
            return;
        }
        gradeAndClose(session, exam, LiveExamSession.Status.AUTO_SUBMITTED);
    }

    private void gradeAndClose(LiveExamSession session, Exam exam, LiveExamSession.Status finalStatus) {
        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExamIdOrderByOrderNumberAsc(exam.getId());

        BigDecimal obtained = BigDecimal.ZERO;
        BigDecimal negativePerWrong = exam.getNegativeMarking();

        for (ExamQuestion eq : examQuestions) {
            String selectedOptionId = session.getAnswers().get(eq.getQuestionId());
            if (selectedOptionId == null) continue;

            Option opt = optionRepository.findById(selectedOptionId).orElse(null);
            if (opt != null && opt.isCorrect()) {
                obtained = obtained.add(eq.getMarks());
            } else {
                obtained = obtained.subtract(negativePerWrong);
            }
        }
        if (obtained.compareTo(BigDecimal.ZERO) < 0) obtained = BigDecimal.ZERO;

        session.setObtainedMarks(obtained);
        session.setStatus(finalStatus);
        session.setSubmittedAt(LocalDateTime.now(BD_ZONE));
        liveSessionRepository.save(session);

        double pct = exam.getTotalMarks().doubleValue() > 0
                ? obtained.doubleValue() / exam.getTotalMarks().doubleValue() * 100
                : 0;
        boolean passed = obtained.compareTo(exam.getPassMarks()) >= 0;

        ExamAttemptHistory history = ExamAttemptHistory.builder()
                .id(UUID.randomUUID().toString())
                .userId(session.getUserId())
                .examId(exam.getId())
                .sessionId(session.getId())
                .attemptNumber(1)
                .obtainedMarks(obtained)
                .totalMarks(exam.getTotalMarks())
                .percentage(BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP))
                .isPassed(passed)
                .submittedAt(session.getSubmittedAt())
                .build();
        attemptHistoryRepository.save(history);

        log.info("Live exam closed: session={}, user={}, exam={}, status={}, marks={}",
                session.getId(), session.getUserId(), exam.getId(), finalStatus, obtained);
    }

    // ============================================
    // 8. RESULT
    // ============================================
    @Transactional(readOnly = true)
    public LiveExamResultResponse getResult(String examId, String userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        LocalDateTime windowEnd = LocalDateTime.of(exam.getExamDate(), LocalTime.of(23, 59, 59));
        if (LocalDateTime.now(BD_ZONE).isBefore(windowEnd)) {
            throw new RuntimeException("Result will be available after the exam window closes at 11:59 PM.");
        }

        LiveExamSession session = liveSessionRepository.findByExamIdAndUserId(examId, userId)
                .orElseThrow(() -> new RuntimeException("No attempt found for this exam."));

        if (session.getStatus() != LiveExamSession.Status.SUBMITTED
                && session.getStatus() != LiveExamSession.Status.AUTO_SUBMITTED) {
            autoSubmit(session, exam);
        }

        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExamIdOrderByOrderNumberAsc(exam.getId());

        List<LiveExamResultResponse.QuestionResultDto> qResults = new ArrayList<>();
        for (ExamQuestion eq : examQuestions) {
            Question q = questionRepository.findById(eq.getQuestionId()).orElse(null);
            if (q == null) continue;

            List<LiveExamResultResponse.QuestionResultDto> qResults = new ArrayList<>();
        for (ExamQuestion eq : examQuestions) {
            Question q = questionRepository.findById(eq.getQuestionId()).orElse(null);
            if (q == null) continue;

            List<Option> options = optionRepository.findAllByQuestionIdOrderByOrderIndex(q.getId());

            Option correct = options.stream().filter(Option::isCorrect).findFirst().orElse(null);
            String selectedId = session.getAnswers().get(q.getId());
            Option selected = selectedId == null ? null : options.stream()
                    .filter(o -> o.getId().equals(selectedId)).findFirst().orElse(null);

            boolean isCorrect = selected != null && selected.isCorrect();
            boolean isSkipped = selectedId == null;
            double marksObtained;
            if (isSkipped) {
                marksObtained = 0.0;
            } else if (isCorrect) {
                marksObtained = eq.getMarks().doubleValue();
            } else {
                marksObtained = -exam.getNegativeMarking().doubleValue();
            }

            qResults.add(LiveExamResultResponse.QuestionResultDto.builder()
                    .questionId(q.getId())
                    .questionText(q.getQuestionText())
                    .userSelectedOptionId(selectedId)
                    .userSelectedOptionText(selected == null ? null : selected.getOptionText())
                    .isCorrect(isCorrect)
                    .isSkipped(isSkipped)
                    .correctOptionId(correct == null ? null : correct.getId())
                    .correctOptionText(correct == null ? null : correct.getOptionText())
                    .explanation(correct == null ? null : correct.getExplanation())
                    .marksObtained(marksObtained)
                    .maxMarks(eq.getMarks().doubleValue())
                    .build());
        }

        List<LiveExamSession> leaderboard = liveSessionRepository.findLeaderboardByExamId(examId);
        int rank = 1;
        Integer myRank = null;
        for (LiveExamSession s : leaderboard) {
            if (s.getUserId().equals(userId)) { myRank = rank; break; }
            rank++;
        }

        double pct = exam.getTotalMarks().doubleValue() > 0
                ? session.getObtainedMarks().doubleValue() / exam.getTotalMarks().doubleValue() * 100
                : 0;

        return LiveExamResultResponse.builder()
                .examId(exam.getId())
                .examName(exam.getName())
                .obtainedMarks(session.getObtainedMarks().doubleValue())
                .totalMarks(exam.getTotalMarks().doubleValue())
                .percentage(Math.round(pct * 100.0) / 100.0)
                .rank(myRank)
                .totalParticipants(leaderboard.size())
                .questions(qResults)
                .build();
    }

    // ============================================
    // 9. LEADERBOARD
    // ============================================
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard(String examId, String userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        LocalDateTime windowEnd = LocalDateTime.of(exam.getExamDate(), LocalTime.of(23, 59, 59));
        if (LocalDateTime.now(BD_ZONE).isBefore(windowEnd)) {
            throw new RuntimeException("Leaderboard will be available after the exam window closes at 11:59 PM.");
        }

        List<LiveExamSession> sessions = liveSessionRepository.findLeaderboardByExamId(examId);
        List<LeaderboardEntryResponse> result = new ArrayList<>();
        int rank = 1;
        for (LiveExamSession s : sessions) {
            double pct = exam.getTotalMarks().doubleValue() > 0
                    ? s.getObtainedMarks().doubleValue() / exam.getTotalMarks().doubleValue() * 100
                    : 0;
            result.add(LeaderboardEntryResponse.builder()
                    .rank(rank++)
                    .userId(s.getUserId())
                    .obtainedMarks(s.getObtainedMarks().doubleValue())
                    .totalMarks(exam.getTotalMarks().doubleValue())
                    .percentage(Math.round(pct * 100.0) / 100.0)
                    .isCurrentUser(s.getUserId().equals(userId))
                    .build());
        }
        return result;
    }

    // ============================================
    // 10. SCHEDULER HOOKS
    // ============================================
    @Transactional
    public void processExpiredSessions() {
        LocalDateTime now = LocalDateTime.now(BD_ZONE);

        LocalDateTime graceCutoff = now.minusMinutes(GRACE_PERIOD_MINUTES);
        List<LiveExamSession> expiredDisconnected =
                liveSessionRepository.findExpiredDisconnectedSessions(graceCutoff);
        for (LiveExamSession s : expiredDisconnected) {
            examRepository.findById(s.getExamId()).ifPresent(exam -> autoSubmit(s, exam));
        }

        List<LiveExamSession> timeExpired =
                liveSessionRepository.findTimeExpiredActiveSessions(now);
        for (LiveExamSession s : timeExpired) {
            examRepository.findById(s.getExamId()).ifPresent(exam -> autoSubmit(s, exam));
        }
    }

    // ============================================
    // PRIVATE HELPERS
    // ============================================
    private LiveExamSession getOwnedSession(String sessionId, String userId) {
        LiveExamSession session = liveSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized for this session.");
        }
        return session;
    }

    private void ensureActive(LiveExamSession session) {
        if (session.getStatus() != LiveExamSession.Status.IN_PROGRESS
                && session.getStatus() != LiveExamSession.Status.DISCONNECTED) {
            throw new RuntimeException("This exam session is no longer active.");
        }
        if (LocalDateTime.now(BD_ZONE).isAfter(session.getExpiresAt())) {
            throw new RuntimeException("Time is up for this exam.");
        }
    }

    private LiveExamStartResponse buildStartResponse(LiveExamSession session, Exam exam) {
        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExamIdOrderByOrderNumberAsc(exam.getId());

        List<LiveQuestionResponse> questions = new ArrayList<>();
        for (ExamQuestion eq : examQuestions) {
            Question q = questionRepository.findById(eq.getQuestionId()).orElse(null);
            if (q == null) continue;

            List<Option> options = optionRepository.findAllByQuestionIdOrderByOrderIndex(q.getId());

            List<LiveQuestionResponse.OptionDto> optionDtos = options.stream()
                    .map(o -> LiveQuestionResponse.OptionDto.builder()
                            .optionId(o.getId())
                            .optionKey(o.getOptionKey())
                            .optionText(o.getOptionText())
                            .optionTextBn(o.getOptionTextBn())
                            .build())
                    .collect(Collectors.toList());

            questions.add(LiveQuestionResponse.builder()
                    .questionId(q.getId())
                    .orderNumber(eq.getOrderNumber())
                    .questionText(q.getQuestionText())
                    .questionTextBn(q.getQuestionTextBn())
                    .marks(eq.getMarks().doubleValue())
                    .options(optionDtos)
                    .selectedOptionId(session.getAnswers().get(q.getId()))
                    .markedForReview(session.getMarkedForReview().contains(q.getId()))
                    .build());
        }

        long remaining = Math.max(0, ChronoUnit.SECONDS.between(LocalDateTime.now(BD_ZONE), session.getExpiresAt()));

        return LiveExamStartResponse.builder()
                .sessionId(session.getId())
                .examId(exam.getId())
                .examName(exam.getName())
                .durationMinutes(exam.getDurationMinutes())
                .startedAt(session.getStartedAt())
                .expiresAt(session.getExpiresAt())
                .remainingSeconds(remaining)
            .negativeMarking(exam.getNegativeMarking())
                .questions(questions)
                .build();
    }
}
