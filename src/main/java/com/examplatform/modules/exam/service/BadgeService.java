package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.entity.UserBadge;
import com.examplatform.modules.exam.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final UserBadgeRepository userBadgeRepository;

    public void awardBadgeIfEarned(String userId, UserBadge.BadgeType badgeType) {
        Optional<UserBadge> existingBadge = userBadgeRepository
                .findByUserIdAndBadgeType(userId, badgeType);

        if (existingBadge.isPresent()) {
            return;
        }

        UserBadge badge = UserBadge.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .badgeType(badgeType)
                .build();

        userBadgeRepository.save(badge);
    }

    public List<UserBadge> getUserBadges(String userId) {
        return userBadgeRepository.findByUserId(userId);
    }

    public int getTotalBadges(String userId) {
        return userBadgeRepository.countByUserId(userId);
    }

    public void checkAndAwardBadges(String userId, int currentStreak, double percentage) {
        if (currentStreak == 7) {
            awardBadgeIfEarned(userId, UserBadge.BadgeType.STREAK_7);
        }
        if (currentStreak == 30) {
            awardBadgeIfEarned(userId, UserBadge.BadgeType.STREAK_30);
        }
        if (currentStreak == 100) {
            awardBadgeIfEarned(userId, UserBadge.BadgeType.STREAK_100);
        }

        if (percentage == 100) {
            awardBadgeIfEarned(userId, UserBadge.BadgeType.PERFECT_SCORE);
        }
    }
}