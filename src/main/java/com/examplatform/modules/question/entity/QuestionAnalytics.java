package com.examplatform.modules.question.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAnalytics {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    private Question question;

    @Column(name = "total_attempts", nullable = false)
    @Builder.Default
    private long totalAttempts = 0;

    @Column(name = "correct_attempts", nullable = false)
    @Builder.Default
    private long correctAttempts = 0;

    @Column(name = "skip_count", nullable = false)
    @Builder.Default
    private long skipCount = 0;

    @Column(name = "avg_time_spent_sec", nullable = false)
    @Builder.Default
    private double avgTimeSpentSec = 0;

    @Column(name = "accuracy_rate", nullable = false)
    @Builder.Default
    private double accuracyRate = 0;

    @Column(name = "difficulty_score_actual")
    private Double difficultyScoreActual;

    @Column(name = "discrimination_index")
    private Double discriminationIndex;

    @Column(name = "last_computed_at")
    private LocalDateTime lastComputedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (totalAttempts > 0) {
            this.accuracyRate = (double) correctAttempts
                    / totalAttempts * 100;
        }
    }
}