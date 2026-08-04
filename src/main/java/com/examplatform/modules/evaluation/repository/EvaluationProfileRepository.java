package com.examplatform.modules.evaluation.repository;

import com.examplatform.modules.evaluation.entity.EvaluationProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationProfileRepository extends JpaRepository<EvaluationProfile, String> {

    Optional<EvaluationProfile> findByName(String name);

    List<EvaluationProfile> findByModelName(String modelName);

    boolean existsByName(String name);
}
