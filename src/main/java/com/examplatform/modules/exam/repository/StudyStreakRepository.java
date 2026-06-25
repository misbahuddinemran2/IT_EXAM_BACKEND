package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.StudyStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyStreakRepository extends JpaRepository<StudyStreak, String> {

    Optional<StudyStreak> findByUserId(String userId);

    @Query("SELECT s FROM StudyStreak s ORDER BY s.currentStreakDays DESC LIMIT 10")
    List<StudyStreak> findTop10CurrentStreaks();

    @Query("SELECT s FROM StudyStreak s ORDER BY s.longestStreakDays DESC LIMIT 10")
    List<StudyStreak> findTop10LongestStreaks();
}