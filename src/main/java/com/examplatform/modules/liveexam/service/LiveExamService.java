package com.examplatform.modules.liveexam.service;

import com.examplatform.modules.exam.entity.Exam;
import com.examplatform.modules.exam.entity.ExamQuestion;
import com.examplatform.modules.exam.repository.ExamAttemptHistoryRepository;
import com.examplatform.modules.exam.repository.ExamQuestionRepository;
import com.examplatform.modules.exam.repository.ExamRepository;
import com.examplatform.modules.exam.entity.ExamAttemptHistory;
import com.examplatform.modules.liveexam.dto.*;
import com.examplatform.modules.liveexam.entity.LiveExamSession;
import com.examplatform.modules.liveexam.repository.LiveExamSessionRepository;
import com.examplatform.modules.question.entity.Option;
import com.examplatform.modules.question.entity.Question;
import com.examplatform.modules.question.repository.OptionRepository;
import com.examplatform.modules.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveExamService {

    private static final int GRACE_PERIOD_MINUTES = 5;

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final LiveExamSessionRepository liveSessionRepository;
    private final ExamAttemptHistoryRepository attemptHistoryRepository;

    // ============================================
    // 1. VISIBILITY — আজকের published live exams
    // ============================================
    @Transactional(readOnly = true)
    public List<Exam> getTodaysLiveExams(String userLevel) {
        LocalDate today = LocalDate.now();
        List<Exam> exams = examRepository.findByPublishStatusAndExamDate(
                Exam.PublishStatus.PUBLISHED, today);

        return exams.stream()
                .filter(e -> isVisibleToUser(e, userLevel))
                .collect(Collectors.toList());
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

        LocalDate today = LocalDate.now();
        if (!exam.getExamDate().equals(today)) {
            throw new RuntimeException("This exam is not scheduled for today.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = LocalDateTime.of(today, LocalTime.of(23, 59, 59));
        if (now.isAfter(windowEnd)) {
            throw new RuntimeException("Exam window for today has closed.");
        }

        // Already started? -> resume instead of creating new
        Optional<LiveExamSession> existing =
                liveSessionRepository.findByExamIdAndUserId(examId, userId);
        if (existing.isPresent()) {
            return resumeInternal(existing.get(), exam);
        }

        // Create new session (unique constraint also guards against race)
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
            // race condition: someone else started same instant -> just resume
            LiveExamSession existingRace = liveSessionRepository
                    .findByExamIdAndUserId(examId, userId)
                    .orElseThrow(() -> new RuntimeException("Could not start or resume exam."));
            return resumeInternal(existingRace, exam);
        }

        return buildStartResponse(session, exam);
    }

    // ============================================
    // 3. RESUME (called on app reopen within grace period)
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
        LocalDateTime now = LocalDateTime.now();

        if (session.getStatus() == LiveExamSession.Status.SUBMITTED
                || session.getStatus() == LiveExamSession.Status.AUTO_SUBMITTED) {
            throw new RuntimeException("This exam attempt is already finished.");
        }

        // Duration সময় শেষ? -> auto submit immediately
        if (now.isAfter(session.getExpiresAt())) {
            autoSubmit(session, exam);
            throw new RuntimeException("Time is up. Your exam has been auto-submitted.");
        }

        // Disconnected obostay a firle asle, grace period check
        if (session.getStatus() == LiveExamSession.Status.DISCONNECTED) {
            long minutesSinceDisconnect = ChronoUnit.MINUTES.between(session.getDisconnectedAt(), now);
            if (minutesSinceDisconnect > GRACE_PERIOD_MINUTES) {
                autoSubmit(session, exam);
                throw new RuntimeException("Grace period expired. Your exam has been auto-submitted.");
            }
            // ফিরে এসেছে সময়ের মধ্যে -> resume
            session.setStatus(LiveExamSession.Status.IN_PROGRESS);
            session.setDisconnectedAt(null);
        }

        session.setLastSeenAt(now);
        liveSessionRepository.save(session);

        return buildStartResponse(session, exam);
    }

    // ============================================
    // 4. HEARTBEAT — client periodically calls this while active
    //    (app on-foreground). Missing heartbeats => mark disconnected
    //    via scheduler.
    // ============================================
    @Transactional
    public void heartbeat(String sessionId, String userId) {
        LiveExamSession session = getOwnedSession(sessionId, userId);
        if (session.getStatus() == LiveExamSession.Status.IN_PROGRESS
                || session.getStatus() == LiveExamSession.Status.DISCONNECTED) {
            session.setLastSeenAt(LocalDateTime.now());
            if (session.getStatus() == LiveExamSession.Status.DISCONNECTED) {
                session.setStatus(LiveExamSession.Status.IN_PROGRESS);
                session.setDisconnectedAt(null);
            }
            liveSessionRepository.save(session);
        }
    }

    // Client app background/close হলে explicit call (optional but recommended)
    @Transactional
    public void markDisconnected(String sessionId, String userId) {
        LiveExamSession session = getOwnedSession(sessionId, userId);
        if (session.getStatus() == LiveExamSession.Status.IN_PROGRESS) {
            session.setStatus(LiveExamSession.Status.DISCONNECTED);
            session.setDisconnectedAt(LocalDateTime.now());
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

        session.setLastSeenAt(LocalDateTime.now());
        liveSessionRepository.save(session);
    }

    // ============================================
    // 6. FINISH (manual submit by user)
    // ============================================
    @Transactional
    public void finishExam(String sessionId, String userId) {
        LiveExamSession session = getOwnedSession(sessionId, userId);
        if (session.getStatus() == LiveExamSession.Status.SUBMITTED
                || session.getStatus() == LiveExamSession.Status.AUTO_SUBMITTED) {
            return; // already done, idempotent
        }
        Exam exam = examRepository.findById(session.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        gradeAndClose(session, exam, LiveExamSession.Status.SUBMITTED);
    }

    // ============================================
    // 7. AUTO-SUBMIT (grace expiry / duration expiry) — used internally + scheduler
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
            if (selectedOptionId == null) continue; // skipped, no marks, no negative

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
        session.setSubmittedAt(LocalDateTime.now());
        liveSessionRepository.save(session);

        // Attempt history তেও save করি (reuse করলাম existing infra)
        double pct = exam.getTotalMarks().doubleValue() > 0
                ? obtained.doubleValue() / exam.getTotalMarks().doubleValue() * 100
                : 0;
        boolean passed = obtained.compareTo(exam.getPassMarks()) >= 0;

        ExamAttemptHistory history = ExamAttemptHistory.builder()
                .id(UUID.randomUUID().toString())
                .userId(session.getUserId())
                .examId(exam.getId())
                .sessionId(session.getId())
                .attemptNumber(1) // live exam = always 1 attempt
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
    // 8. RESULT (time-gated — শুধু exam window শেষ হলে দেখা যাবে)
    // ============================================
    @Transactional(readOnly = true)
    public LiveExamResultResponse getResult(String examId, String userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        LocalDateTime windowEnd = LocalDateTime.of(exam.getExamDate(), LocalTime.of(23, 59, 59));
        if (LocalDateTime.now().isBefore(windowEnd)) {
            throw new RuntimeException("Result will be available after the exam window closes at 11:59 PM.");
        }

        LiveExamSession session = liveSessionRepository.findByExamIdAndUserId(examId, userId)
                .orElseThrow(() -> new RuntimeException("No attempt found for this exam."));

        if (session.getStatus() != LiveExamSession.Status.SUBMITTED
                && session.getStatus() != LiveExamSession.Status.AUTO_SUBMITTED) {
            // safety net: window is over but session never got closed (e.g. scheduler lag)
            autoSubmit(session, exam);
        }

        List<ExamQuestion> examQuestions =
                examQuestionRepository.findByExamIdOrderByOrderNumberAsc(exam.getId());

        List<LiveExamResultResponse.QuestionResultDto> qResults = new ArrayList<>();
        for (ExamQuestion eq : examQuestions) {
            Question q = questionRepository.findById(eq.getQuestionId()).orElse(null);
            if (q == null) continue;

            // ✅ ঠিক করা মেথড নেম
            List<Option> options = optionRepository.findAllByQuestionIdOrderByOrderIndex(q.getId());

            Option correct = options.stream().filter(Option::isCorrect).findFirst().orElse(null);
            String selectedId = session.getAnswers().get(q.getId());
            Option selected = selectedId == null ? null : options.stream()
                    .filter(o -> o.getId().equals(selectedId)).findFirst().orElse(null);

            qResults.add(LiveExamResultResponse.QuestionResultDto.builder()
                    .questionId(q.getId())
                    .questionText(q.getQuestionText())
                    .userSelectedOptionId(selectedId)
                    .userSelectedOptionText(selected == null ? null : selected.getOptionText())
                    .isCorrect(selected != null && selected.isCorrect())
                    .isSkipped(selectedId == null)
                    .correctOptionId(correct == null ? null : correct.getId())
                    .correctOptionText(correct == null ? null : correct.getOptionText())
                    .explanation(correct == null ? null : correct.getExplanation())
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
    // 9. LEADERBOARD (visible alongside result, so also time-gated)
    // ============================================
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard(String examId, String userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        LocalDateTime windowEnd = LocalDateTime.of(exam.getExamDate(), LocalTime.of(23, 59, 59));
        if (LocalDateTime.now().isBefore(windowEnd)) {
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
    // 10. SCHEDULER HOOKS (called from LiveExamScheduler)
    // ============================================
    @Transactional
    public void processExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();

        // A) Grace period পার হয়ে যাওয়া disconnected sessions
        LocalDateTime graceCutoff = now.minusMinutes(GRACE_PERIOD_MINUTES);
        List<LiveExamSession> expiredDisconnected =
                liveSessionRepository.findExpiredDisconnectedSessions(graceCutoff);
        for (LiveExamSession s : expiredDisconnected) {
            examRepository.findById(s.getExamId()).ifPresent(exam -> autoSubmit(s, exam));
        }

        // B) Duration শেষ কিন্তু এখনো active (safety net)
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
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
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

            // ✅ ঠিক করা মেথড নেম
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

        long remaining = Math.max(0, ChronoUnit.SECONDS.between(LocalDateTime.now(), session.getExpiresAt()));

        return LiveExamStartResponse.builder()
                .sessionId(session.getId())
                .examId(exam.getId())
                .examName(exam.getName())
                .durationMinutes(exam.getDurationMinutes())
                .startedAt(session.getStartedAt())
                .expiresAt(session.getExpiresAt())
                .remainingSeconds(remaining)
                .questions(questions)
                .build();
    }
}
