package com.examplatform.modules.taxonomy.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicRequest {

    @NotBlank(message = "Chapter ID is required")
    private String chapterId;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name max 200 characters")
    private String name;

    @Size(max = 200)
    private String nameBn;

    private String description;

    @Min(value = 0)
    private int orderIndex = 0;
}