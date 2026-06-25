package com.examplatform.modules.exam.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PerformanceAnalyticsResponse {

    // Overall Stats
    private int totalExamsGiven;
    private int totalQuestionsAttempted;
    private int totalCorrect;
    private int totalWrong;
    private int totalSkipped;
    private double overallPercentage;
    private int totalTimeSpentMinutes;

    // Subject-wise Performance
    private List<SubjectPerformance> subjectWisePerformance;

    // Difficulty-wise Performance
    private List<DifficultyPerformance> difficultyWisePerformance;

    // Monthly Performance
    private List<MonthlyPerformance> monthlyPerformance;

    // Weak Areas
    private List<WeakTopicData> weakTopics;

    @Data
    @Builder
    public static class SubjectPerformance {
        private String subjectName;
        private String subjectId;
        private int totalAttempts;
        private int correctCount;
        private double percentage;
        private String grade; // A, B, C, D, F
    }

    @Data
    @Builder
    public static class DifficultyPerformance {
        private String difficultyLevel; // Easy, Medium, Hard
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