package com.examplatform.modules.evaluation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromptCreateRequest {
    private String name;
    private String version;
    private String systemPrompt;
    private String userPrompt;
    private String notes;
}
