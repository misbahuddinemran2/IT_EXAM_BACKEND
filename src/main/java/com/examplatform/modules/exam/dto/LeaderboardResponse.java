package com.examplatform.modules.exam.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LeaderboardResponse {

    private List<LeaderboardEntry> leaders;
    private UserRankInfo userRankInfo;

    @Data
    @Builder
    public static class LeaderboardEntry {
        private int rank;
        private String userId;
        private String userName;
        private String userProfileImage;
        private double totalPercentage;
        private int totalExams;
        private int currentStreak;
        private int badges;
    }

    @Data
    @Builder
    public static class UserRankInfo {
        private int userRank;
        private double userPercentage;
        private int totalUsers;
        private String percentileRank;
    }
}