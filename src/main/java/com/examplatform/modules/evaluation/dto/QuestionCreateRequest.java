package com.examplatform.modules.evaluation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionCreateRequest {
    private String datasetId;   // required
    private String questionText;
    private String expectedAnswer;
    private String expectedWriterNames;
    private String difficulty;      // QuestionDifficulty enum name
    private String questionType;    // EvaluationQuestionType enum name
    private String referenceBook;
    private Integer referencePage;
    private String referenceChunkId;
    private String subjectId;
    private String chapterId;
    private String topicId;
}
