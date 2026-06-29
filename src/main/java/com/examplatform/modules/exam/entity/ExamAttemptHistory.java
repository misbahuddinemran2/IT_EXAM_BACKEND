package com.examplatform.modules.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_attempt_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAttemptHistory {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "exam_id", nullable = false, length = 36)
    private String examId;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "attempt_number", nullable = false)
    @Builder.Default
    private int attemptNumber = 1;

    @Column(name = "obtained_marks", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal obtainedMarks = BigDecimal.ZERO;

    @Column(name = "total_marks", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal totalMarks = BigDecimal.ZERO;

    @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal percentage = BigDecimal.ZERO;

    @Column(name = "is_passed", nullable = false)
    @Builder.Default
    private boolean isPassed = false;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}