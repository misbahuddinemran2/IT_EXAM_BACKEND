
package com.examplatform.modules.exam.entity;

import jakarta.persistence.*;
        import lombok.*;
        import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_topic_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamTopicConfig {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "exam_id", nullable = false, length = 36)
    private String examId;

    @Column(name = "subject_id", nullable = false, length = 36)
    private String subjectId;

    @Column(name = "chapter_id", length = 36)
    private String chapterId; // nullable - শুধু topic wise হলে

    @Column(name = "topic_id", length = 36)
    private String topicId;   // nullable - শুধু specific topic হলে

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @Column(name = "marks_per_question", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal marksPerQuestion = BigDecimal.ONE;

    // Difficulty %
    @Column(name = "easy_percent", nullable = false)
    @Builder.Default
    private int easyPercent = 0;

    @Column(name = "medium_percent", nullable = false)
    @Builder.Default
    private int mediumPercent = 0;

    @Column(name = "hard_percent", nullable = false)
    @Builder.Default
    private int hardPercent = 0;

    // Cognitive %
    @Column(name = "remember_percent", nullable = false)
    @Builder.Default
    private int rememberPercent = 0;

    @Column(name = "understand_percent", nullable = false)
    @Builder.Default
    private int understandPercent = 0;

    @Column(name = "apply_percent", nullable = false)
    @Builder.Default
    private int applyPercent = 0;

    @Column(name = "analyze_percent", nullable = false)
    @Builder.Default
    private int analyzePercent = 0;

    @Column(name = "evaluate_percent", nullable = false)
    @Builder.Default
    private int evaluatePercent = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}