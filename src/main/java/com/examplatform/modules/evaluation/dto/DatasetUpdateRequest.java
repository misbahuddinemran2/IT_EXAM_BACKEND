package com.examplatform.modules.evaluation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatasetUpdateRequest {
    private String name;
    private String domain;
    private String language;
    private String description;
}
