package com.examplatform.modules.exam.dto.response;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ExamQuestionDetailResponse {
    private String examQuestionId;
    private String questionId;
    private String questionText;
    private Integer difficultyLevel;
    private double marks;
    private int orderNumber;
}
