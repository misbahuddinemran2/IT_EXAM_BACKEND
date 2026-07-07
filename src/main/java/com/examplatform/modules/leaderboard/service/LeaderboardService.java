package com.examplatform.modules.leaderboard.service;

import com.examplatform.modules.exam.entity.ExamAttemptHistory;
import com.examplatform.modules.exam.repository.ExamAttemptHistoryRepository;
import com.examplatform.modules.exam.repository.ExamRepository;
import com.examplatform.modules.leaderboard.dto.*;
import com.examplatform.modules.leaderboard.entity.LeaderboardSettings;
import com.examplatform.modules.leaderboard.entity.UserLeaderboardStats;
import com.examplatform.modules.leaderboard.entity.UserMonthlyLeaderboardStats;
import com.examplatform.modules.leaderboard.repository.LeaderboardSettingsRepository;
import com.examplatform.modules.leaderboard.repository.UserLeaderboardStatsRepository;
import com.examplatform.modules.leaderboard.repository.UserMonthlyLeaderboardStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private static final Set<String> ELIGIBLE_LEVELS = Set.of(
            "CLASS_9", "NEW_CLASS_10", "SSC", "HSC_1ST_YEAR", "HSC_2ND_YEAR"
    );
    private static final String SETTINGS_ID = "default";
    private static final DateTimeFormatter YM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final JdbcTemplate jdbcTemplate;
    private final LeaderboardSettingsRepository settingsRepository;
    private final UserLeaderboardStatsRepository statsRepository;
    private final UserMonthlyLeaderboardStatsRepository monthlyStatsRepository;
    private final ExamAttemptHistoryRepository attemptHistoryRepository;
    private final ExamRepository examRepository;

    // ============================================
    // TRIGGER — Live exam submit হওয়ার পর কল হবে
    // ============================================
    @Transactional
    public void updateStatsAfterAttempt(String userId, ExamAttemptHistory history) {
        String educationLevel = fetchUserEducationLevel(userId);
        if (!isEligible(educationLevel)) {
            log.info("Leaderboard skip — user {} not eligible (level={})", userId, educationLevel);
            return;
        }

        BigDecimal scorePercent = history.getPercentage(); // ExamAttemptHistory তে already percentage আছে
        LocalDateTime submittedAt = history.getSubmittedAt();
        String yearMonth = submittedAt.toLocalDate().format(YM_FORMAT);

        updateOverallStats(userId, educationLevel, scorePercent);
        updateMonthlyStats(userId, educationLevel, yearMonth, scorePercent);
    }

    private void updateOverallStats(String userId, String educationLevel, BigDecimal scorePercent) {
        UserLeaderboardStats stats = statsRepository.findByUserId(userId)
                .orElseGet(() -> UserLeaderboardStats.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .educationLevel(educationLevel)
                        .totalExamsTaken(0)
                        .totalPoints(BigDecimal.ZERO)
                        .avgScorePercent(BigDecimal.ZERO)
                        .build());

        stats.setEducationLevel(educationLevel); // level পরিবর্তন হলে (promotion) sync থাকবে
        int newCount = stats.getTotalExamsTaken() + 1;
        BigDecimal newTotalPoints = stats.getTotalPoints().add(scorePercent);
        BigDecimal newAvg = newTotalPoints.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

        stats.setTotalExamsTaken(newCount);
        stats.setTotalPoints(newTotalPoints);
        stats.setAvgScorePercent(newAvg);
        stats.setLastUpdatedAt(LocalDateTime.now());

        statsRepository.save(stats);
    }

    private void updateMonthlyStats(String userId, String educationLevel, String yearMonth, BigDecimal scorePercent) {
        UserMonthlyLeaderboardStats stats = monthlyStatsRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .orElseGet(() -> UserMonthlyLeaderboardStats.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .educationLevel(educationLevel)
                        .yearMonth(yearMonth)
                        .examsTakenThisMonth(0)
                        .totalPointsThisMonth(BigDecimal.ZERO)
                        .avgScorePercentThisMonth(BigDecimal.ZERO)
                        .build());

        stats.setEducationLevel(educationLevel);
        int newCount = stats.getExamsTakenThisMonth() + 1;
        BigDecimal newTotalPoints = stats.getTotalPointsThisMonth().add(scorePercent);
        BigDecimal newAvg = newTotalPoints.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

        stats.setExamsTakenThisMonth(newCount);
        stats.setTotalPointsThisMonth(newTotalPoints);
        stats.setAvgScorePercentThisMonth(newAvg);
        stats.setLastUpdatedAt(LocalDateTime.now());

        monthlyStatsRepository.save(stats);
    }

    // ============================================
    // OVERALL LEADERBOARD (student-facing, নিজের level auto-detect)
    // ============================================
    @Transactional(readOnly = true)
    public LeaderboardPageResponse getOverallLeaderboard(String requestingUserId, int page, int size) {
        String userLevel = fetchUserEducationLevel(requestingUserId);
        LeaderboardSettings settings = getSettingsOrDefault();

        if (!settings.isEnabled()) {
            return LeaderboardPageResponse.disabled();
        }
        if (!isEligible(userLevel)) {
            return LeaderboardPageResponse.needsProfileCompletion();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<UserLeaderboardStats> result = statsRepository
                .findByEducationLevelAndTotalExamsTakenGreaterThanEqualOrderByTotalPointsDesc(
                        userLevel, settings.getOverallMinExamsRequired(), pageable);

        List<LeaderboardEntryDto> entries = buildEntries(result.getContent(), requestingUserId, page, size);

        Integer myRank = findMyRank(requestingUserId, entries, result);

        return LeaderboardPageResponse.builder()
                .status("OK")
                .educationLevel(userLevel)
                .entries(entries)
                .totalElements(result.getTotalElements())
                .myRank(myRank)
                .build();
    }

    // ============================================
    // MONTHLY LEADERBOARD
    // ============================================
    @Transactional(readOnly = true)
    public LeaderboardPageResponse getMonthlyLeaderboard(String requestingUserId, String yearMonth, int page, int size) {
        String userLevel = fetchUserEducationLevel(requestingUserId);
        LeaderboardSettings settings = getSettingsOrDefault();

        if (!settings.isEnabled()) {
            return LeaderboardPageResponse.disabled();
        }
        if (!isEligible(userLevel)) {
            return LeaderboardPageResponse.needsProfileCompletion();
        }

        String targetMonth = (yearMonth == null || yearMonth.isBlank())
                ? YearMonth.now().format(YM_FORMAT)
                : yearMonth;

        int requiredExams = resolveMonthlyRequiredExams(settings, targetMonth);

        Pageable pageable = PageRequest.of(page, size);
        Page<UserMonthlyLeaderboardStats> result = monthlyStatsRepository
                .findByEducationLevelAndYearMonthAndExamsTakenThisMonthGreaterThanEqualOrderByTotalPointsThisMonthDesc(
                        userLevel, targetMonth, requiredExams, pageable);

        List<LeaderboardEntryDto> entries = buildMonthlyEntries(result.getContent(), requestingUserId);

        return LeaderboardPageResponse.builder()
                .status("OK")
                .educationLevel(userLevel)
                .yearMonth(targetMonth)
                .requiredExamsThisMonth(requiredExams)
                .entries(entries)
                .totalElements(result.getTotalElements())
                .build();
    }

    // RELATIVE হলে "এই মাসে মোট exam - allowedMissed" ক্যালকুলেট করে
    private int resolveMonthlyRequiredExams(LeaderboardSettings settings, String yearMonth) {
        if (settings.getMonthlyThresholdType() == LeaderboardSettings.ThresholdType.FIXED) {
            return settings.getMonthlyMinExamsRequired();
        }
        YearMonth ym = YearMonth.parse(yearMonth, YM_FORMAT);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int totalExamsThisMonth = examRepository.countByExamDateBetween(start, end);
        int required = totalExamsThisMonth - settings.getMonthlyAllowedMissedExams();
        return Math.max(required, 1); // অন্তত ১টা তো লাগবেই
    }

    // ============================================
    // MY RANK (dedicated endpoint, eligibility message সহ)
    // ============================================
    @Transactional(readOnly = true)
    public MyRankResponse getMyOverallRank(String userId) {
        String userLevel = fetchUserEducationLevel(userId);
        LeaderboardSettings settings = getSettingsOrDefault();

        if (!isEligible(userLevel)) {
            return MyRankResponse.needsProfileCompletion();
        }

        Optional<UserLeaderboardStats> statsOpt = statsRepository.findByUserId(userId);
        if (statsOpt.isEmpty()) {
            return MyRankResponse.builder()
                    .status("NOT_STARTED")
                    .examsTaken(0)
                    .examsNeededMore(settings.getOverallMinExamsRequired())
                    .message("Leaderboard-এ আসতে " + settings.getOverallMinExamsRequired() + "টা Live Exam দিন।")
                    .build();
        }

        UserLeaderboardStats stats = statsOpt.get();
        boolean eligible = stats.getTotalExamsTaken() >= settings.getOverallMinExamsRequired();

        if (!eligible) {
            int needed = settings.getOverallMinExamsRequired() - stats.getTotalExamsTaken();
            return MyRankResponse.builder()
                    .status("NOT_ELIGIBLE")
                    .examsTaken(stats.getTotalExamsTaken())
                    .examsNeededMore(needed)
                    .totalPoints(stats.getTotalPoints())
                    .message("আরও " + needed + "টা Live Exam দিন Leaderboard-এ rank পেতে।")
                    .build();
        }

        int rank = computeRank(userLevel, stats.getTotalPoints(), settings.getOverallMinExamsRequired());

        return MyRankResponse.builder()
                .status("OK")
                .rank(rank)
                .examsTaken(stats.getTotalExamsTaken())
                .totalPoints(stats.getTotalPoints())
                .avgScorePercent(stats.getAvgScorePercent())
                .build();
    }

    private int computeRank(String educationLevel, BigDecimal myPoints, int minExams) {
        Long higherCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_leaderboard_stats WHERE education_level = ? AND total_exams_taken >= ? AND total_points > ?",
                Long.class, educationLevel, minExams, myPoints);
        return (higherCount == null ? 0 : higherCount.intValue()) + 1;
    }

    // ============================================
    // ADMIN SETTINGS
    // ============================================
    @Transactional(readOnly = true)
    public LeaderboardSettings getSettings() {
        return getSettingsOrDefault();
    }

    @Transactional
    public LeaderboardSettings updateSettings(LeaderboardSettingsUpdateRequest req, String adminId) {
        LeaderboardSettings settings = getSettingsOrDefault();
        settings.setOverallMinExamsRequired(req.getOverallMinExamsRequired());
        settings.setMonthlyThresholdType(req.getMonthlyThresholdType());
        settings.setMonthlyMinExamsRequired(req.getMonthlyMinExamsRequired());
        settings.setMonthlyAllowedMissedExams(req.getMonthlyAllowedMissedExams());
        settings.setLevelWiseSeparate(req.isLevelWiseSeparate());
        settings.setEnabled(req.isEnabled());
        settings.setUpdatedByAdminId(adminId);
        return settingsRepository.save(settings);
    }

    private LeaderboardSettings getSettingsOrDefault() {
        return settingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> settingsRepository.save(
                        LeaderboardSettings.builder().id(SETTINGS_ID).build()));
    }

    // ============================================
    // HELPERS
    // ============================================
    private boolean isEligible(String educationLevel) {
        return educationLevel != null && ELIGIBLE_LEVELS.contains(educationLevel);
    }

    private String fetchUserEducationLevel(String userId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT education_level FROM users WHERE id = ?", userId);
            if (rows.isEmpty()) return null;
            return (String) rows.get(0).get("education_level");
        } catch (Exception e) {
            log.warn("Could not fetch education_level for user {}", userId);
            return null;
        }
    }

    private List<LeaderboardEntryDto> buildEntries(List<UserLeaderboardStats> statsList, String requestingUserId, int page, int size) {
        List<LeaderboardEntryDto> entries = new ArrayList<>();
        int rank = page * size + 1;
        for (UserLeaderboardStats s : statsList) {
            Map<String, Object> userInfo = fetchUserNameAndCollege(s.getUserId());
            entries.add(LeaderboardEntryDto.builder()
                    .rank(rank++)
                    .userId(s.getUserId())
                    .userName((String) userInfo.getOrDefault("name", ""))
                    .collegeName((String) userInfo.getOrDefault("college", ""))
                    .totalPoints(s.getTotalPoints())
                    .avgScorePercent(s.getAvgScorePercent())
                    .examsTaken(s.getTotalExamsTaken())
                    .isCurrentUser(s.getUserId().equals(requestingUserId))
                    .build());
        }
        return entries;
    }

    private List<LeaderboardEntryDto> buildMonthlyEntries(List<UserMonthlyLeaderboardStats> statsList, String requestingUserId) {
        List<LeaderboardEntryDto> entries = new ArrayList<>();
        int rank = 1;
        for (UserMonthlyLeaderboardStats s : statsList) {
            Map<String, Object> userInfo = fetchUserNameAndCollege(s.getUserId());
            entries.add(LeaderboardEntryDto.builder()
                    .rank(rank++)
                    .userId(s.getUserId())
                    .userName((String) userInfo.getOrDefault("name", ""))
                    .collegeName((String) userInfo.getOrDefault("college", ""))
                    .totalPoints(s.getTotalPointsThisMonth())
                    .avgScorePercent(s.getAvgScorePercentThisMonth())
                    .examsTaken(s.getExamsTakenThisMonth())
                    .isCurrentUser(s.getUserId().equals(requestingUserId))
                    .build());
        }
        return entries;
    }

    private Map<String, Object> fetchUserNameAndCollege(String userId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT full_name, full_name_bn, institution_name FROM users WHERE id = ?", userId);
            if (rows.isEmpty()) return Map.of();
            Map<String, Object> row = rows.get(0);
            String fullNameBn = (String) row.get("full_name_bn");
            String fullName = (String) row.get("full_name");
            String name = (fullNameBn != null && !fullNameBn.isBlank()) ? fullNameBn : fullName;
            return Map.of("name", name == null ? "" : name, "college", row.getOrDefault("institution_name", ""));
        } catch (Exception e) {
            return Map.of();
        }
    }

    private Integer findMyRank(String userId, List<LeaderboardEntryDto> currentPageEntries, Page<UserLeaderboardStats> page) {
        return currentPageEntries.stream()
                .filter(LeaderboardEntryDto::isCurrentUser)
                .map(LeaderboardEntryDto::getRank)
                .findFirst()
                .orElse(null); // current page এ না থাকলে null — my-rank endpoint আলাদা ভাবে দেখাবে
    }
}
