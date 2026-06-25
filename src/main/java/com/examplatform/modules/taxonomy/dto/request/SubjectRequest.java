package com.examplatform.modules.taxonomy.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name max 100 characters")
    private String name;

    @Size(max = 100, message = "Bengali name max 100 characters")
    private String nameBn;

    @NotBlank(message = "Code is required")
    @Size(max = 20, message = "Code max 20 characters")
    @Pattern(
        regexp = "^[A-Z0-9_]+$",
        message = "Code must be uppercase letters, numbers or underscore"
    )
    private String code;
}