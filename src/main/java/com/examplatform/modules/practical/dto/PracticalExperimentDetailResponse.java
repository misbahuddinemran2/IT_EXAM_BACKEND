package com.examplatform.modules.practical.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PracticalExperimentDetailResponse {
    private String id;
    private String chapterId;
    private String title;
    private String titleBn;
    private String description;

    // Khata
    private String khataType;   // PDF / TEXT / BOTH / null (সেট করা না থাকলে)
    private String pdfUrl;
    private String textContent;

    // Viva
    private List<VivaQuestionDto> vivaQuestions;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VivaQuestionDto {
        private String id;
        private String question;
        private String questionBn;
        private String answer;
    }
}
