package com.examplatform.modules.question.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagResponse {
    private String id;
    private String name;
    private String tagType;
    private String colorCode;
    private int usageCount;
}