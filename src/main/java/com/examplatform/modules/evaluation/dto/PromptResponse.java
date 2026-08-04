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
public class PromptResponse {
    private String id;
    private String name;
    private String version;
    private String systemPrompt;
    private String userPrompt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
