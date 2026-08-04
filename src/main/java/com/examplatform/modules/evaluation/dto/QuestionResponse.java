package com.examplatform.modules.evaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private String id;
    private String datasetId;
    private String questionText;
    private String expectedAnswer;
    private String expectedWriterNames;
    private String difficulty;
    private String questionType;
    private String referenceBook;
    private Integer referencePage;
    private String referenceChunkId;
    private String subjectId;
    private String subjectName;
    private String chapterId;
    private String chapterName;
    private String topicId;
    private String topicName;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
