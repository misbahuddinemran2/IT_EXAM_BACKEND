package com.examplatform.modules.practical.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "practical_viva_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticalVivaQuestion {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "experiment_id", nullable = false, length = 36)
    private String experimentId;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "question_bn", columnDefinition = "TEXT")
    private String questionBn;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "order_number", nullable = false)
    @Builder.Default
    private int orderNumber = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
