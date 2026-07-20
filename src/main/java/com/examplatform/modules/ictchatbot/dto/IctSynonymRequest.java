package com.examplatform.modules.ictchatbot.dto;

import lombok.Data;

@Data
public class IctSynonymRequest {
    private String word;
    private String canonicalWord;
    private Boolean isActive;
}
