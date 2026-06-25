package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.dto.AnalyticsResponse;
import com.examplatform.modules.exam.entity.ExamSession;
import com.examplatform.modules.exam.entity.UserTopicWeakness;
import com.examplatform.modules.exam.repository.ExamSessionRepository;
import com.examplatform.modules.exam.repository.UserTopicWeaknessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ExamSessionRepository examSessionRepository;
    private final UserTopicWeaknessRepository userTopicWeaknessRepository;

    public AnalyticsResponse getAnalytics(String userId) {

        // সব sessions নিন
        List<ExamSession> sessions = examSessionRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        // Overall stats
        int totalExams = sessions.size();
        int totalQuestions = sessions.stream()
                .mapToInt(ExamSession::getTotalQuestions)
                .sum();
        int totalCorrect = sessions.stream()
                .mapToInt(ExamSession::getCorrectCount)
                .sum();
        int totalWrong = sessions.stream()
                .mapToInt(ExamSession::getWrongCount)
                .sum();
        int totalSkipped = sessions.stream()
                .mapToInt(ExamSession::getSkipCount)
                .sum();
        double overallPercentage = totalQuestions > 0
                ? (totalCorrect * 100.0) / totalQuestions
                : 0;
        int totalTimeMinutes = sessions.stream()
                .mapToInt(ExamSession::getTimeSpentSec)
                .sum() / 60;

        // Subject-wise performance
        List<UserTopicWeakness> subjectPerformances = userTopicWeaknessRepository
                .findByUserIdOrderByAccuracyRateDesc(userId);

        List<AnalyticsResponse.SubjectPerformance> subjectData =
                subjectPerformances.stream()
                        .map(perf -> AnalyticsResponse.SubjectPerformance.builder()
                                .subjectName(perf.getExamTypeId() != null ? perf.getExamTypeId() : "Unknown")
                                .totalAttempts(perf.getTotalAttempts())
                                .correctCount(perf.getCorrectAttempts())
                                .percentage(perf.getAccuracyRate() != null
                                        ? perf.getAccuracyRate().doubleValue()
                                        : 0)
                                .grade(getGrade(perf.getAccuracyRate() != null
                                        ? perf.getAccuracyRate().doubleValue()
                                        : 0))
                                .build())
                        .collect(Collectors.toList());

        // Monthly performance
        List<AnalyticsResponse.MonthlyPerformance> monthlyData =
                getMonthlyPerformance(sessions);

        // Weak areas
        List<UserTopicWeakness> weakAreas = userTopicWeaknessRepository
                .findWeakAreasForUser(userId);

        List<AnalyticsResponse.WeakTopicData> weakTopicData =
                weakAreas.stream()
                        .map(w -> AnalyticsResponse.WeakTopicData.builder()
                                .topicName(w.getTopicId())
                                .subjectName(w.getExamTypeId() != null ? w.getExamTypeId() : "Unknown")
                                .totalAttempts(w.getTotalAttempts())
                                .correctCount(w.getCorrectAttempts())
                                .percentage(w.getAccuracyRate() != null
                                        ? w.getAccuracyRate().doubleValue()
                                        : 0)
                                .build())
                        .collect(Collectors.toList());

        return AnalyticsResponse.builder()
                .totalExamsGiven(totalExams)
                .totalQuestionsAttempted(totalQuestions)
                .totalCorrect(totalCorrect)
                .totalWrong(totalWrong)
                .totalSkipped(totalSkipped)
                .overallPercentage(roundToTwoDecimals(overallPercentage))
                .totalTimeSpentMinutes(totalTimeMinutes)
                .subjectWisePerformance(subjectData)
                .monthlyPerformance(monthlyData)
                .weakTopics(weakTopicData)
                .build();
    }

    private List<AnalyticsResponse.MonthlyPerformance>
    getMonthlyPerformance(List<ExamSession> sessions) {

        Map<YearMonth, List<ExamSession>> sessionsByMonth = sessions.stream()
                .filter(s -> s.getCreatedAt() != null)
                .collect(Collectors.groupingBy(s ->
                        YearMonth.from(s.getCreatedAt())));

        return sessionsByMonth.entrySet().stream()
                .map(entry -> {
                    List<ExamSession> monthlySessions = entry.getValue();
                    int examsGiven = monthlySessions.size();
                    int totalCorrect = monthlySessions.stream()
                            .mapToInt(ExamSession::getCorrectCount)
                            .sum();
                    int totalQuestions = monthlySessions.stream()
                            .mapToInt(ExamSession::getTotalQuestions)
                            .sum();
                    double percentage = totalQuestions > 0
                            ? (totalCorrect * 100.0) / totalQuestions
                            : 0;

                    return AnalyticsResponse.MonthlyPerformance.builder()
                            .month(entry.getKey().toString())
                            .examsGiven(examsGiven)
                            .percentage(roundToTwoDecimals(percentage))
                            .questionsAttempted(totalQuestions)
                            .build();
                })
                .sorted(Comparator.comparing(AnalyticsResponse.MonthlyPerformance::getMonth).reversed())
                .collect(Collectors.toList());
    }

    private String getGrade(double percentage) {
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}