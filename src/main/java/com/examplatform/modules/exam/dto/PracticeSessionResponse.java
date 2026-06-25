package com.examplatform.modules.exam.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PracticeSessionResponse {
    private String sessionId;
    private String sessionType;
    private int totalQuestions;
    private int attemptedCount;
    private int remainingCount;
    private String status;
    private String startedAt;
}