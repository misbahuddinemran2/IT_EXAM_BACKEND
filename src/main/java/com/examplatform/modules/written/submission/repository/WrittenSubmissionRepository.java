package com.examplatform.modules.written.submission.repository;

import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WrittenSubmissionRepository extends JpaRepository<WrittenSubmission, String> {

    boolean existsByExamIdAndUserIdAndCycleNumberAndIsPracticeModeFalse(
            String examId, String userId, Integer cycleNumber);

    Optional<WrittenSubmission> findByExamIdAndUserIdAndCycleNumberAndIsPracticeModeFalse(
            String examId, String userId, Integer cycleNumber);

    List<WrittenSubmission> findByExamIdAndUserIdAndIsPracticeModeTrueOrderByAttemptNumberDesc(
            String examId, String userId);

    long countByExamIdAndUserIdAndIsPracticeModeTrue(String examId, String userId);

    List<WrittenSubmission> findByExamIdAndStatus(String examId, SubmissionStatus status);

    List<WrittenSubmission> findByUserIdOrderByCreatedAtDesc(String userId);

    List<WrittenSubmission> findByExamId(String examId);
}