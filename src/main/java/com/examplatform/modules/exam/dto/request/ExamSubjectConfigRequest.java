package com.examplatform.modules.exam.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSubjectConfigRequest {

    private String subjectId;
    private int questionCount;
    private double marksPerQuestion;

    // Difficulty % (তিনটা মিলে 100 হবে)
    private int easyPercent;
    private int mediumPercent;
    private int hardPercent;

    // Cognitive % (মিলে 100 হবে)
    private int rememberPercent;
    private int understandPercent;
    private int applyPercent;
    private int analyzePercent;
    private int evaluatePercent;
}