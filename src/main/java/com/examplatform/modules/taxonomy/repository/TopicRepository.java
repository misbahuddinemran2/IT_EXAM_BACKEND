package com.examplatform.modules.taxonomy.repository;

import com.examplatform.modules.taxonomy.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository
        extends JpaRepository<Topic, String> {

    List<Topic> findAllByChapterIdAndIsActiveOrderByOrderIndex(
            String chapterId, boolean isActive);

    boolean existsByChapterIdAndNameIgnoreCase(
            String chapterId, String name);

    boolean existsByChapterIdAndNameIgnoreCaseAndIdNot(
            String chapterId, String name, String id);
}