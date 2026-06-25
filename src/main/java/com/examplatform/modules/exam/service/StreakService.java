package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.entity.StudyStreak;
import com.examplatform.modules.exam.repository.StudyStreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final StudyStreakRepository studyStreakRepository;

    public void updateStreakOnExamCompletion(String userId) {
        StudyStreak streak = studyStreakRepository.findByUserId(userId)
                .orElse(StudyStreak.builder()
                        .userId(userId)
                        .currentStreakDays(0)
                        .longestStreakDays(0)
                        .totalStudyDays(0)
                        .build());

        LocalDate today = LocalDate.now();
        LocalDate lastActivityDate = streak.getLastActivityDate();

        if (lastActivityDate != null && lastActivityDate.equals(today)) {
            return;
        }

        if (lastActivityDate != null && lastActivityDate.equals(today.minusDays(1))) {
            streak.setCurrentStreakDays(streak.getCurrentStreakDays() + 1);
        } else {
            streak.setCurrentStreakDays(1);
        }

        if (streak.getCurrentStreakDays() > streak.getLongestStreakDays()) {
            streak.setLongestStreakDays(streak.getCurrentStreakDays());
        }

        streak.setLastActivityDate(today);
        streak.setTotalStudyDays(streak.getTotalStudyDays() + 1);

        studyStreakRepository.save(streak);
    }

    public StudyStreak getUserStreak(String userId) {
        return studyStreakRepository.findByUserId(userId)
                .orElse(StudyStreak.builder()
                        .userId(userId)
                        .currentStreakDays(0)
                        .longestStreakDays(0)
                        .totalStudyDays(0)
                        .build());
    }
}