package com.examplatform.modules.examtype.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamTypeResponse {
    private String id;
    private String name;
    private String nameBn;
    private String code;
    private String description;
    private String conductingBody;
    private boolean isActive;
}