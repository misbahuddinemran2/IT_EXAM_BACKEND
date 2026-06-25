package com.examplatform.modules.question.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuestionAnalyticsResponse {
    private String questionId;
    private String questionText;
    private long totalAttempts;
    private long correctAttempts;
    private long skipCount;
    private double accuracyRate;
    private double avgTimeSpentSec;
    private Double difficultyScoreActual;
    private String lastComputedAt;
}