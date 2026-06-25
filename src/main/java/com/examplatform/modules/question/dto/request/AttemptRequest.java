package com.examplatform.modules.question.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttemptRequest {

    @NotBlank(message = "Question ID is required")
    private String questionId;

    @NotBlank(message = "Selected option ID is required")
    private String selectedOptionId;

    private int timeSpentSec = 0;

    private boolean skipped = false;
}