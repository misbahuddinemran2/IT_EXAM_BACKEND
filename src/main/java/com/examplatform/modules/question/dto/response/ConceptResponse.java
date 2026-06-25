package com.examplatform.modules.question.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConceptResponse {
    private String id;
    private String topicId;
    private String topicName;
    private String parentConceptId;
    private String parentConceptName;
    private String name;
    private String nameBn;
    private String description;
    private String conceptType;
    private int difficultyLevel;
    private double importanceScore;
    private boolean isActive;
}