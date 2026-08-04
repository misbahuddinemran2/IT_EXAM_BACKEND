package com.examplatform.modules.evaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// Result list view-এর হালকা row (pagination-friendly)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultSummaryResponse {
    private String id;
    private String questionText;
    private String status;
    private boolean answerFound;
    private BigDecimal semanticSimilarityScore;
    private BigDecimal citationPrecision;
    private Integer responseTimeMs;
}
