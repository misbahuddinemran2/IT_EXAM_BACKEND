package com.examplatform.modules.taxonomy.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopicResponse {
    private String id;
    private String chapterId;
    private String chapterName;
    private String subjectId;
    private String subjectName;
    private String name;
    private String nameBn;
    private String description;
    private int orderIndex;
    private boolean isActive;
}