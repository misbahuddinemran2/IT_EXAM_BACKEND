package com.examplatform.modules.evaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultResponse {
    private String id;
    private String runId;
    private String questionId;
    private String questionText;
    private String expectedAnswer;
    private String generatedAnswer;
    private String responsePath;
    private String matchedWriterNames;
    private String expectedWriterNames;
    private boolean answerFound;
    private boolean fromCache;

    private List<String> retrievedChunkIds;
    private List<Double> retrievedChunkDistances;
    private Double closestChunkDistance;
    private Integer retrievedChunkCount;
    private Integer candidateChunkCount;

    private Integer retrievalLatencyMs;
    private Integer llmLatencyMs;
    private Integer responseTimeMs;

    private String promptVersion;
    private String modelName;
    private Integer tokenInput;
    private Integer tokenOutput;

    private Boolean exactMatch;
    private BigDecimal semanticSimilarityScore;
    private BigDecimal tokenF1Score;

    private BigDecimal citationCoverage;
    private BigDecimal citationPrecision;
    private BigDecimal citationRecall;
    private BigDecimal citationFaithfulness;

    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
