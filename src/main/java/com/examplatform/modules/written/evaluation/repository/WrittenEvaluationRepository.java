package com.examplatform.modules.written.evaluation.repository;

import com.examplatform.modules.written.evaluation.entity.WrittenEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WrittenEvaluationRepository extends JpaRepository<WrittenEvaluation, String> {

    Optional<WrittenEvaluation> findBySubmissionId(String submissionId);

    boolean existsBySubmissionId(String submissionId);
}