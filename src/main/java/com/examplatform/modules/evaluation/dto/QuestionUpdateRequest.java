package com.examplatform.modules.evaluation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionUpdateRequest {
    private String questionText;
    private String expectedAnswer;
    private String expectedWriterNames;
    private String difficulty;
    private String questionType;
    private String referenceBook;
    private Integer referencePage;
    private String referenceChunkId;
    private String subjectId;
    private String chapterId;
    private String topicId;
    private Boolean isActive;
}
