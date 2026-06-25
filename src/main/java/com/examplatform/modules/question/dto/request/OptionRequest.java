package com.examplatform.modules.question.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionRequest {

    @NotBlank(message = "Option key is required")
    @Pattern(regexp = "^[A-D]$",
             message = "Option key must be A, B, C or D")
    private String optionKey;

    @NotBlank(message = "Option text is required")
    private String optionText;

    private String optionTextBn;

    @JsonProperty("isCorrect")
    private boolean isCorrect = false;

    private String explanation;

    private String explanationBn;

    private int orderIndex = 0;

    // Bulk Upload এর জন্য
    public void setCorrect(boolean correct) {
        this.isCorrect = correct;
    }

    public boolean isCorrect() {
        return isCorrect;
    }
}