package com.examplatform.modules.exam.dto;

import lombok.Data;

@Data
public class PracticeStartRequest {
    private String subjectId;
    private String topicId;
    private String chapterId;
    private int questionCount = 10;
    private Integer difficultyLevel;
}