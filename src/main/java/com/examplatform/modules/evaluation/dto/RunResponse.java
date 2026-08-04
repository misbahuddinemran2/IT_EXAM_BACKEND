package com.examplatform.modules.evaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunResponse {
    private String id;
    private String datasetId;
    private String datasetName;
    private String datasetVersion;
    private String profileId;
    private String profileName;
    private String promptId;
    private String promptName;
    private Map<String, Object> configurationSnapshot;
    private String status;
    private Integer totalQuestions;
    private Integer processedQuestions;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String triggeredByAdminId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
