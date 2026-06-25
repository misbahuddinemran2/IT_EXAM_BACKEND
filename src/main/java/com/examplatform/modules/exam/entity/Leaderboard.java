package com.examplatform.modules.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "leaderboard", indexes = {
        @Index(name = "idx_leader_examtype", columnList = "exam_type_id,period_type"),
        @Index(name = "idx_leader_sexam", columnList = "special_exam_id,period_type"),
        @Index(name = "idx_leader_user", columnList = "user_id"),
        @Index(name = "idx_leader_rank", columnList = "rank_position")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leaderboard {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "exam_type_id", length = 36)
    private String examTypeId;

    @Column(name = "special_exam_id", length = 36)
    private String specialExamId;

    @Column(name = "period_type")
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @Column(name = "total_score")
    private BigDecimal totalScore;

    @Column(name = "total_sessions")
    private int totalSessions;

    @Column(name = "total_attempts")
    private int totalAttempts;

    @Column(name = "accuracy_rate")
    private BigDecimal accuracyRate;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(name = "percentile")
    private BigDecimal percentile;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "computed_at")
    private LocalDateTime computedAt;

    public enum PeriodType {
        DAILY,
        WEEKLY,
        MONTHLY,
        ALL_TIME
    }

    @PrePersist
    protected void onCreate() {
        computedAt = LocalDateTime.now();
    }
}