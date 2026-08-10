package com.examplatform.modules.practical.dto;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PracticalExperimentResponse {
    private String id;
    private String chapterId;
    private String title;
    private String titleBn;
    private String description;
}
