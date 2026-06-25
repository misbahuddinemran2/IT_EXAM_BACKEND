package com.examplatform.modules.question.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConceptCreateRequest {

    @NotBlank(message = "Topic ID is required")
    private String topicId;

    private String parentConceptId;

    @NotBlank(message = "Name is required")
    @Size(max = 300)
    private String name;

    @Size(max = 300)
    private String nameBn;

    private String description;

    private String conceptType = "DEFINITION";

    @Min(1) @Max(5)
    private int difficultyLevel = 3;

    @DecimalMin("0.0") @DecimalMax("1.0")
    private double importanceScore = 0.50;
}