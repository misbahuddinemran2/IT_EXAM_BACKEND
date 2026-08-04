package com.examplatform.modules.evaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetResponse {
    private String id;
    private String name;
    private String domain;
    private String language;
    private String description;
    private String createdByAdminId;
    private String createdByAdminName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
