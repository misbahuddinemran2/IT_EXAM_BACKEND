package com.examplatform.modules.liveexam.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SubmitLiveAnswerRequest {
    private String questionId;
    private String selectedOptionId; // null = clear answer
    private Boolean markForReview;   // optional toggle
}
