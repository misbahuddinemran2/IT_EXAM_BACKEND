package com.examplatform.modules.exam.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExamResultResponse {
    private String sessionId;
    private String sessionType;
    private int totalQuestions;
    private int attemptedCount;
    private int correctCount;
    private int wrongCount;
    private int skipCount;
    private double score;
    private double percentage;
    private int timeSpentSec;
    private boolean isPassed;
    private List<QuestionReviewResponse> questionReviews;

    @Data
    @Builder
    public static class QuestionReviewResponse {
        private String questionId;
        private String questionText;
        private String selectedOptionId;
        private String selectedOptionText;
        private String correctOptionId;
        private String correctOptionText;
        private boolean Correct;
        private boolean Skipped;
        private String explanation;
    }
}