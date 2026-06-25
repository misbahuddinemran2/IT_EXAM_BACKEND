package com.examplatform.modules.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_badges", indexes = {
        @Index(name = "idx_badge_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBadge {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "badge_type")
    @Enumerated(EnumType.STRING)
    private BadgeType badgeType;

    @Column(name = "earned_at")
    private LocalDateTime earnedAt;

    public enum BadgeType {
        STREAK_7,
        STREAK_30,
        STREAK_100,
        FIRST_EXAM,
        PERFECT_SCORE,
        TOP_10,
        TOP_1,
        BATTLE_WINNER,
        FAST_LEARNER,
        CONSISTENT
    }

    @PrePersist
    protected void onCreate() {
        if (earnedAt == null) {
            earnedAt = LocalDateTime.now();
        }
    }
}