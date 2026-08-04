package com.examplatform.modules.evaluation.repository;

import com.examplatform.modules.evaluation.entity.EvaluationPrompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvaluationPromptRepository extends JpaRepository<EvaluationPrompt, String> {

    Optional<EvaluationPrompt> findByNameAndVersion(String name, String version);

    List<EvaluationPrompt> findByName(String name);

    boolean existsByNameAndVersion(String name, String version);
}
