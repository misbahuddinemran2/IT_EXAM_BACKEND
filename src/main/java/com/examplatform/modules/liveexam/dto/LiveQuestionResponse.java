package com.examplatform.modules.liveexam.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LiveQuestionResponse {
    private String questionId;
    private int orderNumber;
    private String questionText;
    private String questionTextBn;
    private double marks;
    private List<OptionDto> options;
    private String selectedOptionId; // pre-filled if resuming
    private boolean markedForReview;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OptionDto {
        private String optionId;
        private String optionKey;
        private String optionText;
        private String optionTextBn;
        // isCorrect / explanation পাঠানো হবে না এখানে — শুধু result এ
    }
}
