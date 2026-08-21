// পাথ: src/main/java/com/examplatform/modules/liveexam/dto/RevisionResultResponse.java
package com.examplatform.modules.liveexam.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RevisionResultResponse {
    private int totalQuestions;
    private int correctCount;
    private int wrongCount;
    private int skipCount;
    private double accuracyRate; // negative marking নেই, শুধু correct/total

    private List<QuestionResultDto> questions;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuestionResultDto {
        private String questionId;
        private String questionText;
        private String selectedOptionId;
        private String selectedOptionText;
        private boolean isCorrect;
        private boolean isSkipped;
        private String correctOptionId;
        private String correctOptionText;
        private String explanation;
    }
}
