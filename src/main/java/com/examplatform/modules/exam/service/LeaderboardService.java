package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.dto.LeaderboardResponse;
import com.examplatform.modules.exam.entity.ExamSession;
import com.examplatform.modules.exam.entity.Leaderboard;
import com.examplatform.modules.exam.entity.StudyStreak;
import com.examplatform.modules.exam.repository.ExamSessionRepository;
import com.examplatform.modules.exam.repository.LeaderboardRepository;
import com.examplatform.modules.exam.repository.StudyStreakRepository;
import com.examplatform.modules.exam.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final ExamSessionRepository examSessionRepository;
    private final StudyStreakRepository studyStreakRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final LeaderboardRepository leaderboardRepository;

    public LeaderboardResponse getLeaderboard(String userId) {

        // সব users এর stats নিন
        List<UserLeaderboardStats> allUserStats = getAllUserStats();

        // Sort by percentage descending
        allUserStats.sort((a, b) -> Double.compare(
                b.getPercentage(), a.getPercentage()));

        // Create leaderboard entries (top 100)
        List<LeaderboardResponse.LeaderboardEntry> topEntries = new ArrayList<>();
        for (int i = 0; i < Math.min(100, allUserStats.size()); i++) {
            UserLeaderboardStats stats = allUserStats.get(i);
            topEntries.add(LeaderboardResponse.LeaderboardEntry.builder()
                    .rank(i + 1)
                    .userId(stats.getUserId())
                    .userName(stats.getUserName())
                    .userProfileImage(stats.getProfileImage())
                    .totalPercentage(roundToTwoDecimals(stats.getPercentage()))
                    .totalExams(stats.getTotalExams())
                    .currentStreak(stats.getCurrentStreak())
                    .badges(stats.getBadges())
                    .build());
        }

        // Current user rank info
        LeaderboardResponse.UserRankInfo userRankInfo =
                getUserRankInfo(userId, allUserStats);

        return LeaderboardResponse.builder()
                .leaders(topEntries)
                .userRankInfo(userRankInfo)
                .build();
    }

    private List<UserLeaderboardStats> getAllUserStats() {

        // সব unique users নিন
        List<String> allUserIds = examSessionRepository.findAllDistinctUserIds();

        List<UserLeaderboardStats> stats = new ArrayList<>();

        for (String userId : allUserIds) {
            List<ExamSession> sessions = examSessionRepository
                    .findByUserIdOrderByCreatedAtDesc(userId);

            if (sessions.isEmpty()) continue;

            int totalExams = sessions.size();
            int totalCorrect = sessions.stream()
                    .mapToInt(ExamSession::getCorrectCount)
                    .sum();
            int totalQuestions = sessions.stream()
                    .mapToInt(ExamSession::getTotalQuestions)
                    .sum();
            double percentage = totalQuestions > 0
                    ? (totalCorrect * 100.0) / totalQuestions
                    : 0;

            StudyStreak streak = studyStreakRepository.findByUserId(userId)
                    .orElse(StudyStreak.builder()
                            .currentStreakDays(0)
                            .build());

            int badgeCount = userBadgeRepository.countByUserId(userId);

            stats.add(UserLeaderboardStats.builder()
                    .userId(userId)
                    .userName("User_" + userId.substring(0, Math.min(5, userId.length())))
                    .profileImage("")
                    .percentage(percentage)
                    .totalExams(totalExams)
                    .currentStreak(streak.getCurrentStreakDays())
                    .badges(badgeCount)
                    .build());
        }

        return stats;
    }

    private LeaderboardResponse.UserRankInfo getUserRankInfo(String userId,
                                                             List<UserLeaderboardStats> allStats) {

        Optional<UserLeaderboardStats> userStat = allStats.stream()
                .filter(s -> s.getUserId().equals(userId))
                .findFirst();

        if (userStat.isEmpty()) {
            return LeaderboardResponse.UserRankInfo.builder()
                    .userRank(0)
                    .userPercentage(0)
                    .totalUsers(allStats.size())
                    .percentileRank("Not Ranked")
                    .build();
        }

        int rank = allStats.indexOf(userStat.get()) + 1;
        int percentile = (int) ((rank * 100.0) / allStats.size());
        String percentileRank = "Top " + (100 - percentile) + "%";

        return LeaderboardResponse.UserRankInfo.builder()
                .userRank(rank)
                .userPercentage(roundToTwoDecimals(userStat.get().getPercentage()))
                .totalUsers(allStats.size())
                .percentileRank(percentileRank)
                .build();
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @lombok.Data
    @lombok.Builder
    private static class UserLeaderboardStats {
        private String userId;
        private String userName;
        private String profileImage;
        private double percentage;
        private int totalExams;
        private int currentStreak;
        private int badges;
    }
}