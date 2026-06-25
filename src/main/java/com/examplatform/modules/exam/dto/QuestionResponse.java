package com.examplatform.modules.exam.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionResponse {
    private String questionId;
    private String questionText;
    private String questionTextBn;
    private String questionType;
    private int estimatedTimeSec;
    private int questionNumber;
    private int totalQuestions;
    private List<OptionResponse> options;

    @Data
    @Builder
    public static class OptionResponse {
        private String optionId;
        private String optionKey;
        private String optionText;
        private String optionTextBn;
    }
}