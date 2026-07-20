package com.examplatform.modules.ictchatbot.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class IctSynonymResponse {
    private UUID id;
    private String word;
    private String canonicalWord;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
