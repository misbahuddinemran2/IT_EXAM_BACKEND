package com.examplatform.modules.practical.repository;

import com.examplatform.modules.practical.entity.PracticalExperiment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PracticalExperimentRepository extends JpaRepository<PracticalExperiment, String> {
    List<PracticalExperiment> findByChapterIdOrderByOrderNumberAsc(String chapterId);
    List<PracticalExperiment> findByChapterIdAndIsActiveTrueOrderByOrderNumberAsc(String chapterId);
}
