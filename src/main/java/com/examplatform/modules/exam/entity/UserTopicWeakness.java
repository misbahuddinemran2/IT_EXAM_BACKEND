package com.examplatform.modules.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "user_topic_weakness", indexes = {
        @Index(name = "idx_weakness_user", columnList = "user_id"),
        @Index(name = "idx_weakness_score", columnList = "weakness_score")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTopicWeakness {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "topic_id", length = 36, nullable = false)
    private String topicId;

    @Column(name = "exam_type_id", length = 36)
    private String examTypeId;

    @Column(name = "total_attempts")
    private int totalAttempts;

    @Column(name = "correct_attempts")
    private int correctAttempts;

    @Column(name = "accuracy_rate")
    private BigDecimal accuracyRate;

    @Column(name = "weakness_score")
    private BigDecimal weaknessScore;

    @Column(name = "avg_time_spent_sec")
    private BigDecimal avgTimeSpentSec;

    @Column(name = "last_attempted_at")
    private LocalDateTime lastAttemptedAt;

    @Column(name = "last_computed_at")
    private LocalDateTime lastComputedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}