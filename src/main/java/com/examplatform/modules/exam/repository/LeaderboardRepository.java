package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, String> {

    List<Leaderboard> findByPeriodTypeOrderByRankPositionAsc(Leaderboard.PeriodType periodType);

    Optional<Leaderboard> findByUserIdAndPeriodType(String userId, Leaderboard.PeriodType periodType);

    @Query("SELECT l FROM Leaderboard l WHERE l.periodType = ?1 ORDER BY l.rankPosition ASC LIMIT 100")
    List<Leaderboard> findTop100(Leaderboard.PeriodType periodType);
}