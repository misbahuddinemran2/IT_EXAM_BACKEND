package com.examplatform.modules.evaluation.repository;

import com.examplatform.modules.evaluation.entity.EvaluationRun;
import com.examplatform.modules.evaluation.enums.EvaluationRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationRunRepository extends JpaRepository<EvaluationRun, String> {

    List<EvaluationRun> findByDatasetId(String datasetId);

    List<EvaluationRun> findByDatasetIdOrderByCreatedAtDesc(String datasetId);

    List<EvaluationRun> findByProfileId(String profileId);

    List<EvaluationRun> findByStatus(EvaluationRunStatus status);

    List<EvaluationRun> findByDatasetIdAndStatus(String datasetId, EvaluationRunStatus status);

    // Multi-model comparison: একই dataset-এর একাধিক run পাশাপাশি দেখার জন্য
    List<EvaluationRun> findByIdInOrderByCreatedAtAsc(List<String> ids);
}
