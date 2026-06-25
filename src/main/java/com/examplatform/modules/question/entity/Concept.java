package com.examplatform.modules.question.entity;

import com.examplatform.common.entity.BaseEntity;
import com.examplatform.modules.taxonomy.entity.Topic;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "concepts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Concept extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_concept_id")
    private Concept parentConcept;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(name = "name_bn", length = 300)
    private String nameBn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "concept_type", nullable = false, length = 20)
    private ConceptType conceptType = ConceptType.DEFINITION;

    @Column(name = "difficulty_level", nullable = false)
    private int difficultyLevel = 3;

    @Column(name = "importance_score", nullable = false)
    private double importanceScore = 0.50;

    @Column(name = "embedding_vector", columnDefinition = "TEXT")
    private String embeddingVector;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    public enum ConceptType {
        DEFINITION, PROCESS, FORMULA, PRINCIPLE, FACT
    }
}