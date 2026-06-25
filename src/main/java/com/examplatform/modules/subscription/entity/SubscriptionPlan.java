package com.examplatform.modules.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "name_bn")
    private String nameBn;

    @Column(name = "plan_code", nullable = false, unique = true)
    private String planCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    @Column(name = "price_bdt", nullable = false)
    private double priceBdt;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "max_practice_per_day", nullable = false)
    private int maxPracticePerDay;

    @Column(name = "max_mock_tests_per_month", nullable = false)
    private int maxMockTestsPerMonth;

    @Column(name = "has_detailed_analytics", nullable = false)
    private boolean hasDetailedAnalytics;

    @Column(name = "has_weak_topic_detection", nullable = false)
    private boolean hasWeakTopicDetection;

    @Column(name = "has_leaderboard", nullable = false)
    private boolean hasLeaderboard;

    @Column(name = "has_battle_exam", nullable = false)
    private boolean hasBattleExam;

    @Column(name = "has_written_exam", nullable = false)
    private boolean hasWrittenExam;

    @Column(name = "has_ai_recommendation", nullable = false)
    private boolean hasAiRecommendation;

    @Column(name = "has_certificate", nullable = false)
    private boolean hasCertificate;

    @Column(name = "has_offline_access", nullable = false)
    private boolean hasOfflineAccess;

    @Column(name = "has_live_exam", nullable = false)
    private boolean hasLiveExam;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "display_order")
    private int displayOrder; // ✅ এটা add করা হয়েছে

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PlanType {
        FREE, MONTHLY, YEARLY, CUSTOM
    }
}