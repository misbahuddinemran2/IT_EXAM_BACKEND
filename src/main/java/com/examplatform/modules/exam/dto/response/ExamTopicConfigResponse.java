package com.examplatform.modules.exam.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamTopicConfigResponse {

    private String id;
    private String subjectId;
    private String subjectName;
    private String chapterId;
    private String chapterName;
    private String topicId;
    private String topicName;

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