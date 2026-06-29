
package com.examplatform.modules.exam.entity;

import jakarta.persistence.*;
        import lombok.*;
        import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamQuestion {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "exam_id", nullable = false, length = 36)
    private String examId;

    @Column(name = "question_id", nullable = false, length = 36)
    private String questionId;

    @Column(name = "marks", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal marks = BigDecimal.ONE;

    @Column(name = "order_number", nullable = false)
    @Builder.Default
    private int orderNumber = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}