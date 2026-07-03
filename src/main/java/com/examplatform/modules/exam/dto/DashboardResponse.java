package com.examplatform.modules.exam.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private String userId;
    private String userName;
    private int totalExams;
    private int totalCorrect;
    private int totalWrong;
    private int totalSkipped;
    private int totalQuestions;
    private double totalPercentage;
    private int currentStreak;
    private int longestStreak;

    private PerformanceSummary performanceSummary;
    private List<RecentExamData> recentExams;
    private List<WeakAreaData> weakAreas;
    private int rankPosition;
    private int totalUsers;

    @Data
    @Builder
    public static class PerformanceSummary {
        private double thisMonthPercentage;
        private double lastMonthPercentage;
        private int percentageChange;
        private String performanceTrend; // UP, DOWN, STABLE
    }

    @Data
    @Builder
    public static class RecentExamData {
        private String sessionId;
        private String examType;
        private int totalQuestions;
        private int correctCount;
        private double percentage;
        private String status;
        private long completedAt;
    }

    @Data
    @Builder
    public static class WeakAreaData {
        private String subjectName;
        private String topicName;
        private int correctCount;
        private int totalAttempts;
        private double percentage;
        private String difficulty;
    }
}
