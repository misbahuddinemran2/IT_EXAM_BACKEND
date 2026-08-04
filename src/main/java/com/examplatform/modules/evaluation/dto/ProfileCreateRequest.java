package com.examplatform.modules.evaluation.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProfileCreateRequest {
    private String name;
    private String modelName;
    private String embeddingModel;
    private Integer topK;
    private BigDecimal similarityThreshold;
    private BigDecimal temperature;
    private String chunkStrategy;
    private Integer maxTokens;
}
