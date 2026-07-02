package com.examplatform.modules.liveexam.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LiveExamResultResponse {
    private String examId;
    private String examName;
    private double obtainedMarks;
    private double totalMarks;
    private double percentage;
    private Integer rank;
    private int totalParticipants;
    private List<QuestionResultDto> questions;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuestionResultDto {
        private String questionId;
        private String questionText;
        private String userSelectedOptionId;
        private String userSelectedOptionText;
        private boolean isCorrect;
        private boolean isSkipped;
        private String correctOptionId;
        private String correctOptionText;
        private String explanation;
        private double marksObtained;
        private double maxMarks;
    }
}
