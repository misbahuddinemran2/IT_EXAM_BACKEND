package com.examplatform.modules.evaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Run list / comparison টেবিলে হালকা row
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunSummaryResponse {
    private String id;
    private String datasetName;
    private String profileName;
    private String modelName;
    private String status;
    private Integer totalQuestions;
    private Integer processedQuestions;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
