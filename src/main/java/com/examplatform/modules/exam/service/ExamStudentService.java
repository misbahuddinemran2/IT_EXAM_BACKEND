package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.dto.response.*;
import com.examplatform.modules.exam.entity.*;
import com.examplatform.modules.exam.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamStudentService {

    private final ExamRepository examRepository;
    private final ExamAttemptHistoryRepository attemptHistoryRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final JdbcTemplate jdbcTemplate;

    // ============================================
    // GET AVAILABLE EXAMS FOR STUDENT
    // ============================================
    public List<AvailableExamResponse> getAvailableExams(String userId) {
        List<Exam> publishedExams = examRepository
                .findByPublishStatusOrderByExamDateAsc(Exam.PublishStatus.PUBLISHED);

        return publishedExams.stream()
                .map(exam -> buildAvailableExamResponse(exam, userId))
                .collect(Collectors.toList());
    }

    // ============================================
    // GET AVAILABLE EXAMS BY TYPE
    // ============================================
    public List<AvailableExamResponse> getAvailableExamsByType(
            String userId, String examType) {

        List<Exam> exams = examRepository
                .findByPublishStatusAndExamTypeOrderByExamDateAsc(
                        Exam.PublishStatus.PUBLISHED,
                        Exam.ExamType.valueOf(examType)
                );

        return exams.stream()
                .map(exam -> buildAvailableExamResponse(exam, userId))
                .collect(Collectors.toList());
    }

    // ============================================
    // GET SINGLE EXAM DETAIL (Student)
    // ============================================
    public AvailableExamResponse getExamDetail(String examId, String userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException(
                        "Exam not found: " + examId
                ));

        if (!Exam.PublishStatus.PUBLISHED.equals(exam.getPublishStatus())) {
            throw new RuntimeException("This exam is not available.");
        }

        return buildAvailableExamResponse(exam, userId);
    }

    // ============================================
    // CHECK CAN USER ATTEMPT
    // ============================================
    public boolean canUserAttempt(String examId, String userId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException(
                        "Exam not found: " + examId
                ));

        // Published check
        if (!Exam.PublishStatus.PUBLISHED.equals(exam.getPublishStatus())) {
            return false;
        }

        // Time check
        String status = getExamStatus(exam);
        if (!"AVAILABLE".equals(status)) {
            return false;
        }

        // Premium check
        if (exam.isPremiumOnly() && !isUserPremium(userId)) {
            return false;
        }

        // Attempt limit check
        if (exam.getMaxAttempts() != null) {
            long usedAttempts = attemptHistoryRepository
                    .countByUserIdAndExamId(userId, examId);
            return usedAttempts < exam.getMaxAttempts();
        }

        return true;
    }

    // ============================================
    // GET ATTEMPT HISTORY (User)
    // ============================================
    public List<ExamAttemptHistoryResponse> getUserAttemptHistory(String userId) {
        return attemptHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::buildAttemptHistoryResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // GET ATTEMPT HISTORY FOR SPECIFIC EXAM
    // ============================================
    public List<ExamAttemptHistoryResponse> getUserExamAttemptHistory(
            String userId, String examId) {

        return attemptHistoryRepository
                .findByUserIdAndExamIdOrderByAttemptNumberAsc(userId, examId)
                .stream()
                .map(this::buildAttemptHistoryResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // SAVE ATTEMPT HISTORY
    // (ExamSession complete হলে call করবে)
    // ============================================
    public void saveAttemptHistory(String userId,
                                   String examId,
                                   String sessionId,
                                   double obtainedMarks,
                                   double totalMarks,
                                   boolean isPassed) {

        // কত নম্বর attempt
        long previousAttempts = attemptHistoryRepository
                .countByUserIdAndExamId(userId, examId);

        double percentage = totalMarks > 0
                ? (obtainedMarks / totalMarks) * 100
                : 0;

        ExamAttemptHistory history = ExamAttemptHistory.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .examId(examId)
                .sessionId(sessionId)
                .attemptNumber((int) previousAttempts + 1)
                .obtainedMarks(BigDecimal.valueOf(obtainedMarks))
                .totalMarks(BigDecimal.valueOf(totalMarks))
                .percentage(BigDecimal.valueOf(percentage)
                        .setScale(2, java.math.RoundingMode.HALF_UP))
                .isPassed(isPassed)
                .submittedAt(LocalDateTime.now())
                .build();

        attemptHistoryRepository.save(history);
        log.info("Attempt history saved: user={}, exam={}, attempt={}",
                userId, examId, history.getAttemptNumber());
    }

    // ============================================
    // PRIVATE — HELPERS
    // ============================================

    private AvailableExamResponse buildAvailableExamResponse(
            Exam exam, String userId) {

        // Attempt info
        long attemptsUsed = attemptHistoryRepository
                .countByUserIdAndExamId(userId, exam.getId());

        Long attemptsRemaining = null;
        if (exam.getMaxAttempts() != null) {
            attemptsRemaining = Math.max(0L,
                    exam.getMaxAttempts() - attemptsUsed);
        }

        boolean canAttempt = canAttemptCheck(
                exam, userId, attemptsUsed, attemptsRemaining
        );

        // Best score
        Optional<BigDecimal> bestPct = attemptHistoryRepository
                .findBestPercentageByUserAndExam(userId, exam.getId());

        boolean hasPassed = attemptHistoryRepository
                .hasUserPassedExam(userId, exam.getId());

        String examStatus = getExamStatus(exam);

        return AvailableExamResponse.builder()
                .id(exam.getId())
                .name(exam.getName())
                .examType(exam.getExamType().name())
                .description(exam.getDescription())
                .totalQuestions(exam.getTotalQuestions())
                .totalMarks(exam.getTotalMarks().doubleValue())
                .passMarks(exam.getPassMarks().doubleValue())
                .negativeMarking(exam.getNegativeMarking().doubleValue())
                .durationMinutes(exam.getDurationMinutes())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .attemptsAllowed(exam.getMaxAttempts() == null
                        ? "Unlimited"
                        : String.valueOf(exam.getMaxAttempts()))
                .attemptsUsed(attemptsUsed)
                .attemptsRemaining(attemptsRemaining)
                .canAttempt(canAttempt)
                .examStatus(examStatus)
                .bestPercentage(bestPct.map(BigDecimal::doubleValue).orElse(null))
                .hasPassed(hasPassed)
                .isPremiumOnly(exam.isPremiumOnly())
                .build();
    }

    // Attempt করা যাবে কিনা check
    private boolean canAttemptCheck(Exam exam, String userId,
                                    long attemptsUsed,
                                    Long attemptsRemaining) {
        // Time check
        if (!"AVAILABLE".equals(getExamStatus(exam))) return false;

        // Premium check
        if (exam.isPremiumOnly() && !isUserPremium(userId)) return false;

        // Attempt limit check
        if (attemptsRemaining != null && attemptsRemaining <= 0) return false;

        return true;
    }

    // Exam এর time status
    private String getExamStatus(Exam exam) {
        if (exam.getExamDate() == null
                || exam.getStartTime() == null
                || exam.getEndTime() == null) {
            return "AVAILABLE"; // Schedule নেই মানে সবসময় available
        }

        LocalDateTime startDt = LocalDateTime.of(
                exam.getExamDate(), exam.getStartTime()
        );
        LocalDateTime endDt = LocalDateTime.of(
                exam.getExamDate(), exam.getEndTime()
        );
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(startDt)) return "NOT_YET_AVAILABLE";
        if (now.isAfter(endDt))    return "EXPIRED";
        return "AVAILABLE";
    }

    // Premium user check
    private boolean isUserPremium(String userId) {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                    "SELECT id FROM user_subscriptions " +
                            "WHERE user_id = ? " +
                            "AND status = 'ACTIVE' " +
                            "AND expires_at > NOW() " +
                            "LIMIT 1",
                    userId
            );
            return !result.isEmpty();
        } catch (Exception e) {
            log.warn("Premium check failed for user: {}", userId);
            return false;
        }
    }

    private ExamAttemptHistoryResponse buildAttemptHistoryResponse(
            ExamAttemptHistory history) {

        // Exam name fetch
        String examName = "";
        String examType = "";
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                    "SELECT name, exam_type FROM exams WHERE id = ?",
                    history.getExamId()
            );
            if (!result.isEmpty()) {
                examName = (String) result.get(0).get("name");
                examType = (String) result.get(0).get("exam_type");
            }
        } catch (Exception e) {
            log.warn("Could not fetch exam name: {}", history.getExamId());
        }

        return ExamAttemptHistoryResponse.builder()
                .id(history.getId())
                .examId(history.getExamId())
                .examName(examName)
                .examType(examType)
                .sessionId(history.getSessionId())
                .attemptNumber(history.getAttemptNumber())
                .obtainedMarks(history.getObtainedMarks().doubleValue())
                .totalMarks(history.getTotalMarks().doubleValue())
                .percentage(history.getPercentage().doubleValue())
                .isPassed(history.isPassed())
                .submittedAt(history.getSubmittedAt())
                .createdAt(history.getCreatedAt())
                .build();
    }
}