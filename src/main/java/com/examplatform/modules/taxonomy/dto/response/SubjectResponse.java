package com.examplatform.modules.taxonomy.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubjectResponse {
    private String id;
    private String name;
    private String nameBn;
    private String code;
    private boolean isActive;
    private String createdAt;
}