package com.examplatform.modules.exam.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AnalyticsResponse {

    private int totalExamsGiven;
    private int totalQuestionsAttempted;
    private int totalCorrect;
    private int totalWrong;
    private int totalSkipped;
    private double overallPercentage;
    private int totalTimeSpentMinutes;

    private List<SubjectPerformance> subjectWisePerformance;
    private List<DifficultyPerformance> difficultyWisePerformance;
    private List<MonthlyPerformance> monthlyPerformance;
    private List<WeakTopicData> weakTopics;

    @Data
    @Builder
    public static class SubjectPerformance {
        private String subjectName;
        private int totalAttempts;
        private int correctCount;
        private double percentage;
        private String grade;
    }

    @Data
    @Builder
    public static class DifficultyPerformance {
        private String difficultyLevel;
        private int totalQuestions;
        private int correctCount;
        private double percentage;
    }

    @Data
    @Builder
    public static class MonthlyPerformance {
        private String month;
        private int examsGiven;
        private double percentage;
        private int questionsAttempted;
    }

    @Data
    @Builder
    public static class WeakTopicData {
        private String topicName;
        private String subjectName;
        private int totalAttempts;
        private int correctCount;
        private double percentage;
    }
}