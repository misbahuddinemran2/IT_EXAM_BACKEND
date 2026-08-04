package com.examplatform.modules.evaluation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatasetCreateRequest {
    private String name;
    private String domain;      // DatasetDomain enum name হিসেবে পাঠাতে হবে
    private String language;    // default "bn"
    private String description;
}
