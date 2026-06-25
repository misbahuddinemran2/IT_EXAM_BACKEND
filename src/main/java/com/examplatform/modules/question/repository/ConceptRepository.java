package com.examplatform.modules.question.repository;

import com.examplatform.modules.question.entity.Concept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConceptRepository
        extends JpaRepository<Concept, String> {

    List<Concept> findAllByTopicIdAndIsActiveOrderByName(
            String topicId, boolean isActive);

    @Query("SELECT c FROM Concept c WHERE c.isActive = true " +
           "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Concept> searchByName(@Param("search") String search);

    boolean existsByTopicIdAndNameIgnoreCase(
            String topicId, String name);

    boolean existsByTopicIdAndNameIgnoreCaseAndIdNot(
            String topicId, String name, String id);
}