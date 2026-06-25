package com.examplatform.modules.exam.dto;

import lombok.Data;

@Data
public class SubmitAnswerRequest {
    private String questionId;
    private String selectedOptionId;
    private boolean isSkipped = false;
    private int timeSpentSec;
}