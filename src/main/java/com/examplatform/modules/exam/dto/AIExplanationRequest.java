package com.examplatform.modules.exam.dto;

import lombok.Data;

@Data
public class AIExplanationRequest {
    private String questionId;
    private String questionText;
    private String selectedAnswerId;
    private String correctAnswerId;
    private String language; // "bn" or "en"
}