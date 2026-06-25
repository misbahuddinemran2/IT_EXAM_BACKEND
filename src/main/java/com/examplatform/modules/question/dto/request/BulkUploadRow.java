package com.examplatform.modules.question.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BulkUploadRow {
    private int rowNumber;
    private String questionText;
    private String questionType;
    private String difficultyLevel;
    private String cognitiveLevel;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption;
    private String explanationA;
    private String explanationB;
    private String explanationC;
    private String explanationD;
    private String sourceReference;
    private String yearAppeared;

    @Builder.Default
    private boolean valid = true;

    @Builder.Default
    private String errorMessage = null;
}