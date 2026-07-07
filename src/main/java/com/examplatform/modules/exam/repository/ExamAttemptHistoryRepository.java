package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.ExamAttemptHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttemptHistoryRepository extends JpaRepository<ExamAttemptHistory, String> {

    // User এর specific exam এর সব attempt
    List<ExamAttemptHistory> findByUserIdAndExamIdOrderByAttemptNumberAsc(
            String userId, String examId
    );

    // User এর সব attempt history
    List<ExamAttemptHistory> findByUserIdOrderByCreatedAtDesc(String userId);

    // User কতবার attempt করেছে
    long countByUserIdAndExamId(String userId, String examId);

    // User এর latest attempt
    Optional<ExamAttemptHistory> findTopByUserIdAndExamIdOrderByAttemptNumberDesc(
            String userId, String examId
    );
    
// এই মেথডগুলো তোমার existing ExamAttemptHistoryRepository ইন্টারফেসে যোগ করো:

int countByUserId(String userId);

int countByUserIdAndSubmittedAtBetween(String userId, LocalDateTime start, LocalDateTime end);

int countBySubmittedAtBetween(LocalDateTime start, LocalDateTime end); // মাসে মোট কতগুলো attempt হয়েছে (RELATIVE threshold হিসাবের জন্য, exam count না attempt count)
    // Exam এর সব attempt (leaderboard এর জন্য)
    List<ExamAttemptHistory> findByExamIdOrderByPercentageDesc(String examId);

    // Pass করেছে কিনা check
    @Query("SELECT COUNT(h) > 0 FROM ExamAttemptHistory h " +
            "WHERE h.userId = :userId AND h.examId = :examId AND h.isPassed = true")
    boolean hasUserPassedExam(
            @Param("userId") String userId,
            @Param("examId") String examId
    );

    // Exam এর best score
    @Query("SELECT MAX(h.percentage) FROM ExamAttemptHistory h " +
            "WHERE h.userId = :userId AND h.examId = :examId")
    Optional<java.math.BigDecimal> findBestPercentageByUserAndExam(
            @Param("userId") String userId,
            @Param("examId") String examId
    );
}
