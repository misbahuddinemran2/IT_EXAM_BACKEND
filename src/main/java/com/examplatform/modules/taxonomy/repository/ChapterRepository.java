package com.examplatform.modules.taxonomy.repository;

import com.examplatform.modules.taxonomy.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository
        extends JpaRepository<Chapter, String> {

    List<Chapter> findAllBySubjectIdAndIsActiveOrderByOrderIndex(
            String subjectId, boolean isActive);

    boolean existsBySubjectIdAndNameIgnoreCase(
            String subjectId, String name);

    boolean existsBySubjectIdAndNameIgnoreCaseAndIdNot(
            String subjectId, String name, String id);
}