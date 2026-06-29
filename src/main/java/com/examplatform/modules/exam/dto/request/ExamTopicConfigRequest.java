package com.examplatform.modules.exam.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamTopicConfigRequest {

    private String subjectId;   // required
    private String chapterId;   // optional
    private String topicId;     // optional

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