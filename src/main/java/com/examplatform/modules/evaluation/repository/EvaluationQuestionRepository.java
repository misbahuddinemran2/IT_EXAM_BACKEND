package com.examplatform.modules.evaluation.repository;

import com.examplatform.modules.evaluation.entity.EvaluationQuestion;
import com.examplatform.modules.evaluation.enums.EvaluationQuestionType;
import com.examplatform.modules.evaluation.enums.QuestionDifficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EvaluationQuestionRepository extends JpaRepository<EvaluationQuestion, String> {

    List<EvaluationQuestion> findByDatasetId(String datasetId);

    List<EvaluationQuestion> findByDatasetIdAndActiveTrue(String datasetId);

    List<EvaluationQuestion> findByDatasetIdAndDifficulty(String datasetId, QuestionDifficulty difficulty);

    List<EvaluationQuestion> findByDatasetIdAndQuestionType(String datasetId, EvaluationQuestionType questionType);

    long countByDatasetId(String datasetId);

    long countByDatasetIdAndActiveTrue(String datasetId);

    @Transactional
    void deleteByDatasetId(String datasetId);
}
