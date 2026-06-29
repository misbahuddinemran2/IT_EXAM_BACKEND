package com.examplatform.modules.exam.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamStatsResponse {

    private long totalExams;
    private long publishedExams;
    private long draftExams;
    private long archivedExams;

    // Type wise count
    private long dailyExams;
    private long weeklyExams;
    private long revisionExams;
    private long subjectWiseExams;
    private long mixedExams;

    // Attempt stats
    private long totalAttempts;
    private double overallAvgPercentage;
}