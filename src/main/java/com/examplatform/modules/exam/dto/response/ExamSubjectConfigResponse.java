package com.examplatform.modules.exam.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSubjectConfigResponse {

    private String id;
    private String subjectId;
    private String subjectName;     // join করে আনবো
    private int questionCount;
    private double marksPerQuestion;

    // Difficulty %
    private int easyPercent;
    private int mediumPercent;
    private int hardPercent;

    // Cognitive %
    private int rememberPercent;
    private int understandPercent;
    private int applyPercent;
    private int analyzePercent;
    private int evaluatePercent;
}