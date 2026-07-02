package com.examplatform.modules.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {
    @Id
    @Column(name = "id", length = 36)
    private String id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "exam_code", length = 50)
    private String examCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 30)
    private ExamType examType;
    @Enumerated(EnumType.STRING)
    @Column(name = "publish_status", nullable = false, length = 20)
    @Builder.Default
    private PublishStatus publishStatus = PublishStatus.DRAFT;
    // Marks & Duration
    @Column(name = "total_questions", nullable = false)
    @Builder.Default
    private int totalQuestions = 0;
    @Column(name = "total_marks", nullable = false, precision = 8, scale = 2)
    private BigDecimal totalMarks;
    @Column(name = "pass_marks", nullable = false, precision = 8, scale = 2)
    private BigDecimal passMarks;
    @Column(name = "negative_marking", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal negativeMarking = BigDecimal.ZERO;
    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;
    // Schedule
    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    // Attempt Control
    @Column(name = "max_attempts")
    private Integer maxAttempts; // NULL = unlimited
    // Settings
    @Column(name = "allow_review", nullable = false)
    @Builder.Default
    private boolean allowReview = true;
    @Column(name = "shuffle_questions", nullable = false)
    @Builder.Default
    private boolean shuffleQuestions = false;
    @Column(name = "shuffle_options", nullable = false)
    @Builder.Default
    private boolean shuffleOptions = false;
    @Column(name = "show_result_after_submit", nullable = false)
    @Builder.Default
    private boolean showResultAfterSubmit = true;
    // Access
    @Column(name = "is_premium_only", nullable = false)
    @Builder.Default
    private boolean isPremiumOnly = false;

    // কোন education level(s) এই exam দেখতে পারবে ("ALL" = সবাই)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_levels", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<String> targetLevels = List.of("ALL");

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    // Audit
    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;
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
    // ============================================
    // ENUMS
    // ============================================
    public enum ExamType {
        DAILY,
        WEEKLY,
        REVISION,
        SUBJECT_WISE,
        CHAPTER_WISE,
        TOPIC_WISE,
        MIXED,
        SPECIAL
    }
    public enum PublishStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }
}
