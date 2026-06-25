package com.examplatform.modules.exam.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerResultResponse {
    private boolean isCorrect;
    private String correctOptionId;
    private String explanation;
    private String explanationBn;
    private int attemptedCount;
    private int remainingCount;
}