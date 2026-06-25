package com.examplatform.modules.question.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuestionResponse {
    private String id;
    private String questionText;
    private String questionTextBn;
    private String questionType;
    private String language;
    private String subjectId;
    private String subjectName;
    private String chapterId;
    private String chapterName;
    private String topicId;
    private String topicName;
    private int difficultyLevel;
    private String cognitiveLevel;
    private int estimatedTimeSec;
    private String sourceReference;
    private Integer yearAppeared;
    private String status;
    private List<OptionResponse> options;
    private List<ConceptResponse> concepts;
    private List<TagResponse> tags;
    private List<String> examTypeIds;
    private String createdAt;
}