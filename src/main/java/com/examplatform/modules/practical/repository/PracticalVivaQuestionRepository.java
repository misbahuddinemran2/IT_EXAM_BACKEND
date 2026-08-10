package com.examplatform.modules.practical.repository;

import com.examplatform.modules.practical.entity.PracticalVivaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticalVivaQuestionRepository extends JpaRepository<PracticalVivaQuestion, String> {
    List<PracticalVivaQuestion> findByExperimentIdOrderByOrderNumberAsc(String experimentId);
    void deleteByExperimentId(String experimentId);
}
