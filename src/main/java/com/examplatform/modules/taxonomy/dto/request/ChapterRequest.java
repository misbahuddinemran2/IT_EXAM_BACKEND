package com.examplatform.modules.taxonomy.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChapterRequest {

    @NotBlank(message = "Subject ID is required")
    private String subjectId;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name max 200 characters")
    private String name;

    @Size(max = 200)
    private String nameBn;

    @Min(value = 0, message = "Order index must be 0 or more")
    private int orderIndex = 0;
}