package com.examplatform.modules.leaderboard.repository;

import com.examplatform.modules.leaderboard.entity.UserLeaderboardStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLeaderboardStatsRepository extends JpaRepository<UserLeaderboardStats, String> {

    Optional<UserLeaderboardStats> findByUserId(String userId);

    Page<UserLeaderboardStats> findByEducationLevelAndTotalExamsTakenGreaterThanEqualOrderByTotalPointsDesc(
            String educationLevel, int minExams, Pageable pageable);
}
