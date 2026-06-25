package com.examplatform.modules.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "user_performance_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPerformanceSummary {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false, unique = true)
    private String userId;

    @Column(name = "total_sessions")
    private int totalSessions;

    @Column(name = "total_questions_seen")
    private int totalQuestionsSeen;

    @Column(name = "total_correct")
    private int totalCorrect;

    @Column(name = "overall_accuracy")
    private BigDecimal overallAccuracy;

    @Column(name = "total_study_time_min")
    private int totalStudyTimeMin;

    @Column(name = "avg_score_per_exam")
    private BigDecimal avgScorePerExam;

    @Column(name = "best_score")
    private BigDecimal bestScore;

    @Column(name = "best_score_exam")
    private String bestScoreExam;

    @Column(name = "strongest_topic_id", length = 36)
    private String strongestTopicId;

    @Column(name = "weakest_topic_id", length = 36)
    private String weakestTopicId;

    @Column(name = "battles_played")
    private int battlesPlayed;

    @Column(name = "battles_won")
    private int battlesWon;

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