package com.examplatform.modules.question.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    private String tagType = "CUSTOM";

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$",
             message = "Color must be valid hex e.g. #FF5733")
    private String colorCode = "#6366f1";
}