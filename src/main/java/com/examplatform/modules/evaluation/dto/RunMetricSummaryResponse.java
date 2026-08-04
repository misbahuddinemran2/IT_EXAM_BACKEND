package com.examplatform.modules.evaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// একটা run-এর aggregate metric summary (কোনো টেবিলে persist হয় না,
// evaluation_result থেকে on-demand গণনা করে বানানো হবে)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunMetricSummaryResponse {
    private String runId;
    private String modelName;
    private long totalQuestions;
    private long exactMatchCount;
    private BigDecimal exactMatchRate;
    private BigDecimal avgSemanticSimilarity;
    private BigDecimal avgTokenF1;
    private BigDecimal avgCitationPrecision;
    private BigDecimal avgCitationRecall;
    private BigDecimal avgRetrievalLatencyMs;
    private BigDecimal avgLlmLatencyMs;
    private BigDecimal avgResponseTimeMs;
    private BigDecimal avgTokenInput;
    private BigDecimal avgTokenOutput;
    private BigDecimal cacheHitRate;
    private BigDecimal successRate;
}
