package com.examplatform.modules.evaluation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromptUpdateRequest {
    private String systemPrompt;
    private String userPrompt;
    private String notes;
}
