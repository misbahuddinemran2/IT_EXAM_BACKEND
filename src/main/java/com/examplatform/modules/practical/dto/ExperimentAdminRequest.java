package com.examplatform.modules.practical.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ExperimentAdminRequest {
    private String chapterId;
    private String title;
    private String titleBn;
    private String description;
    private boolean isActive;
    private List<String> targetSessions;
    private int orderNumber;

    // Khata
    private String khataType; // PDF / TEXT / BOTH
    private String pdfUrl;
    private String textContent;

    // Viva
    private List<VivaItem> vivaQuestions;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class VivaItem {
        private String question;
        private String questionBn;
        private String answer;
        private int orderNumber;
    }
}
