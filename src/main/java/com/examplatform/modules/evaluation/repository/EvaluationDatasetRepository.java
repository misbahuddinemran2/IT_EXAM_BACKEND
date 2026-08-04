package com.examplatform.modules.evaluation.repository;

import com.examplatform.modules.evaluation.entity.EvaluationDataset;
import com.examplatform.modules.evaluation.enums.DatasetDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationDatasetRepository extends JpaRepository<EvaluationDataset, String> {

    List<EvaluationDataset> findByDomain(DatasetDomain domain);

    List<EvaluationDataset> findByLanguage(String language);

    List<EvaluationDataset> findByDomainAndLanguage(DatasetDomain domain, String language);

    List<EvaluationDataset> findByNameContainingIgnoreCase(String name);
}
