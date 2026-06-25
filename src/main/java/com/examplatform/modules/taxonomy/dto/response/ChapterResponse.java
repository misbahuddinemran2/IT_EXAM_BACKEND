package com.examplatform.modules.taxonomy.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChapterResponse {
    private String id;
    private String subjectId;
    private String subjectName;
    private String name;
    private String nameBn;
    private int orderIndex;
    private boolean isActive;
}