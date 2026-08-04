package com.examplatform.modules.evaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String id;
    private String name;
    private String modelName;
    private String embeddingModel;
    private Integer topK;
    private BigDecimal similarityThreshold;
    private BigDecimal temperature;
    private String chunkStrategy;
    private Integer maxTokens;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
