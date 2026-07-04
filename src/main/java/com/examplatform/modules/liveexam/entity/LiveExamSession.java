package com.examplatform.modules.liveexam.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "live_exam_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveExamSession {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "exam_id", nullable = false, length = 36)
    private String examId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.IN_PROGRESS;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;

    @Column(name = "disconnected_at")
    private LocalDateTime disconnectedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answers", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, String> answers = new java.util.HashMap<>(); // questionId -> optionId

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "marked_for_review", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<String> markedForReview = new java.util.ArrayList<>();

    @Column(name = "obtained_marks", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal obtainedMarks = BigDecimal.ZERO;

    @Column(name = "total_marks", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal totalMarks = BigDecimal.ZERO;

    @Column(name = "cycle_number", nullable = false)
@Builder.Default
private int cycleNumber = 1;
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

    public enum Status {
        IN_PROGRESS,     // active, user answering
        DISCONNECTED,    // grace period running (5 min)
        SUBMITTED,       // user manually finished / grace expired but was already done
        AUTO_SUBMITTED   // system auto-submitted (grace period or exam window expired)
    }
}
