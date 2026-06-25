package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.dto.DashboardResponse;
import com.examplatform.modules.exam.entity.ExamSession;
import com.examplatform.modules.exam.entity.StudyStreak;
import com.examplatform.modules.exam.entity.UserTopicWeakness;
import com.examplatform.modules.exam.repository.ExamSessionRepository;
import com.examplatform.modules.exam.repository.StudyStreakRepository;
import com.examplatform.modules.exam.repository.UserTopicWeaknessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExamSessionRepository examSessionRepository;
    private final StudyStreakRepository studyStreakRepository;
    private final UserTopicWeaknessRepository userTopicWeaknessRepository;

    public DashboardResponse getDashboard(String userId, String userName) {

        // সব exam sessions নিন
        List<ExamSession> sessions = examSessionRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        // Overall stats calculate করুন
        int totalExams = sessions.size();
        int totalCorrect = sessions.stream()
                .mapToInt(ExamSession::getCorrectCount)
                .sum();
        int totalQuestions = sessions.stream()
                .mapToInt(ExamSession::getTotalQuestions)
                .sum();
        double totalPercentage = totalQuestions > 0
                ? (totalCorrect * 100.0) / totalQuestions
                : 0;

        // Streak info
        StudyStreak streak = studyStreakRepository.findByUserId(userId)
                .orElse(StudyStreak.builder()
                        .userId(userId)
                        .currentStreakDays(0)
                        .longestStreakDays(0)
                        .build());

        // Performance summary
        DashboardResponse.PerformanceSummary performanceSummary =
                calculatePerformanceSummary(userId, sessions);

        // Recent exams (last 5)
        List<DashboardResponse.RecentExamData> recentExams = sessions.stream()
                .limit(5)
                .map(session -> DashboardResponse.RecentExamData.builder()
                        .sessionId(session.getId())
                        .examType(session.getSessionType() != null ? session.getSessionType().name() : "PRACTICE")
                        .totalQuestions(session.getTotalQuestions())
                        .correctCount(session.getCorrectCount())
                        .percentage(session.getPercentage())
                        .status(session.isPassed() ? "PASS" : "FAIL")
                        .completedAt(session.getCompletedAt() != null
                                ? session.getCompletedAt()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                                : System.currentTimeMillis())
                        .build())
                .collect(Collectors.toList());

        // Weak areas (bottom 3)
        List<UserTopicWeakness> weakAreas = userTopicWeaknessRepository
                .findWeakAreasForUser(userId)
                .stream()
                .limit(3)
                .collect(Collectors.toList());

        List<DashboardResponse.WeakAreaData> weakAreaData = weakAreas.stream()
                .map(weakness -> DashboardResponse.WeakAreaData.builder()
                        .subjectName(weakness.getExamTypeId() != null ? weakness.getExamTypeId() : "Unknown")
                        .topicName(weakness.getTopicId())
                        .correctCount(weakness.getCorrectAttempts())
                        .totalAttempts(weakness.getTotalAttempts())
                        .percentage(weakness.getAccuracyRate() != null
                                ? weakness.getAccuracyRate().doubleValue()
                                : 0)
                        .difficulty(weakness.getWeaknessScore() != null && weakness.getWeaknessScore().doubleValue() > 0.6
                                ? "Hard"
                                : "Medium")
                        .build())
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .userId(userId)
                .userName(userName)
                .totalExams(totalExams)
                .totalCorrect(totalCorrect)
                .totalPercentage(roundToTwoDecimals(totalPercentage))
                .currentStreak(streak.getCurrentStreakDays())
                .longestStreak(streak.getLongestStreakDays())
                .performanceSummary(performanceSummary)
                .recentExams(recentExams)
                .weakAreas(weakAreaData)
                .rankPosition(calculateRankPosition(userId, totalPercentage))
                .totalUsers(calculateTotalUsers())
                .build();
    }

    private DashboardResponse.PerformanceSummary calculatePerformanceSummary(
            String userId, List<ExamSession> allSessions) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1);
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);
        LocalDateTime lastMonthEnd = monthStart.minusDays(1);

        double thisMonthPercentage = calculateMonthlyPercentage(
                allSessions, monthStart, now);
        double lastMonthPercentage = calculateMonthlyPercentage(
                allSessions, lastMonthStart, lastMonthEnd);

        int change = (int) (thisMonthPercentage - lastMonthPercentage);
        String trend = change > 0 ? "UP" : change < 0 ? "DOWN" : "STABLE";

        return DashboardResponse.PerformanceSummary.builder()
                .thisMonthPercentage(roundToTwoDecimals(thisMonthPercentage))
                .lastMonthPercentage(roundToTwoDecimals(lastMonthPercentage))
                .percentageChange(change)
                .performanceTrend(trend)
                .build();
    }

    private double calculateMonthlyPercentage(List<ExamSession> sessions,
                                              LocalDateTime start, LocalDateTime end) {

        List<ExamSession> monthlySessions = sessions.stream()
                .filter(s -> s.getCreatedAt() != null
                        && s.getCreatedAt().isAfter(start)
                        && s.getCreatedAt().isBefore(end))
                .collect(Collectors.toList());

        if (monthlySessions.isEmpty()) return 0;

        int totalCorrect = monthlySessions.stream()
                .mapToInt(ExamSession::getCorrectCount)
                .sum();
        int totalQuestions = monthlySessions.stream()
                .mapToInt(ExamSession::getTotalQuestions)
                .sum();

        return totalQuestions > 0 ? (totalCorrect * 100.0) / totalQuestions : 0;
    }

    private int calculateRankPosition(String userId, double totalPercentage) {
        // এটি leaderboard থেকে আসবে
        return 1;
    }

    private int calculateTotalUsers() {
        // User count থেকে আসবে
        return 100;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}