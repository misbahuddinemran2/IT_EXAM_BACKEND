package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.dto.DashboardResponse;
import com.examplatform.modules.exam.entity.ExamAttemptHistory;
import com.examplatform.modules.exam.entity.ExamSession;
import com.examplatform.modules.exam.entity.StudyStreak;
import com.examplatform.modules.exam.entity.UserTopicWeakness;
import com.examplatform.modules.exam.repository.ExamAttemptHistoryRepository;
import com.examplatform.modules.exam.repository.ExamSessionRepository;
import com.examplatform.modules.exam.repository.StudyStreakRepository;
import com.examplatform.modules.exam.repository.UserTopicWeaknessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExamSessionRepository examSessionRepository;
    private final ExamAttemptHistoryRepository attemptHistoryRepository;
    private final StudyStreakRepository studyStreakRepository;
    private final UserTopicWeaknessRepository userTopicWeaknessRepository;
    private final JdbcTemplate jdbcTemplate;

    public DashboardResponse getDashboard(String userId, String userName) {

        // ===== Regular exam sessions =====
        List<ExamSession> sessions = examSessionRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        int regularExams = sessions.size();
        int regularCorrect = sessions.stream().mapToInt(ExamSession::getCorrectCount).sum();
        int regularWrong = sessions.stream().mapToInt(ExamSession::getWrongCount).sum();
        int regularSkipped = sessions.stream().mapToInt(ExamSession::getSkipCount).sum();
        int regularQuestions = sessions.stream().mapToInt(ExamSession::getTotalQuestions).sum();

        // ===== Live exam attempts (from exam_attempt_history) =====
        List<ExamAttemptHistory> liveAttempts = attemptHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        int liveExams = liveAttempts.size();
        int liveCorrect = 0;
        int liveWrong = 0;
        int liveSkipped = 0;
        int liveQuestions = 0;

        for (ExamAttemptHistory attempt : liveAttempts) {
            boolean published = isResultPublished(attempt.getExamId());

            liveQuestions += attempt.getTotalQuestions();
            liveSkipped += attempt.getSkipCount();

            // correct/wrong শুধু result publish হওয়ার পরেই যোগ হবে
            if (published) {
                liveCorrect += attempt.getCorrectCount();
                liveWrong += attempt.getWrongCount();
            }
        }

        // ===== Combined totals =====
        int totalExams = regularExams + liveExams;
        int totalCorrect = regularCorrect + liveCorrect;
        int totalWrong = regularWrong + liveWrong;
        int totalSkipped = regularSkipped + liveSkipped;
        int totalQuestions = regularQuestions + liveQuestions;

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

        // Performance summary (regular exam sessions অনুযায়ী, আগের মতোই)
        DashboardResponse.PerformanceSummary performanceSummary =
                calculatePerformanceSummary(userId, sessions);

        // Recent exams (last 5, শুধু regular sessions থেকে — আগের মতোই)
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
                .totalWrong(totalWrong)
                .totalSkipped(totalSkipped)
                .totalQuestions(totalQuestions)
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

    // Live exam এর exam_date চেক করে result publish হয়েছে কিনা বের করে
    // (রাত ১১:৫৯টার পর publish হয় — LiveExamService/ExamStudentService এর একই নিয়ম)
    private boolean isResultPublished(String examId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT exam_date FROM exams WHERE id = ?", examId);
            if (rows.isEmpty() || rows.get(0).get("exam_date") == null) {
                return true; // exam_date না থাকলে regular/practice ধরনের, সবসময় published
            }
            Object dateObj = rows.get(0).get("exam_date");
            LocalDate examDate = (dateObj instanceof java.sql.Date)
                    ? ((java.sql.Date) dateObj).toLocalDate()
                    : LocalDate.parse(dateObj.toString());

            LocalDateTime windowEnd = LocalDateTime.of(examDate, LocalTime.of(23, 59, 59));
            return LocalDateTime.now().isAfter(windowEnd);
        } catch (Exception e) {
            log.warn("Could not check result publish status for exam: {}", examId);
            return true; // fail-safe: error হলে published ধরে নেওয়া (dashboard যেন ভেঙে না যায়)
        }
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
        return 1;
    }

    private int calculateTotalUsers() {
        return 100;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
