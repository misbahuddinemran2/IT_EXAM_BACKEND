package com.examplatform.modules.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_question_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionAttempt {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "selected_option_id")
    private String selectedOptionId;

    @Column(name = "is_correct")
    private boolean isCorrect;

    @Column(name = "is_skipped")
    private boolean isSkipped;

    @Column(name = "time_spent_sec")
    private int timeSpentSec;

    @Column(name = "confidence_level")
    private Integer confidenceLevel;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @PrePersist
    protected void onCreate() {
        answeredAt = LocalDateTime.now();
    }
}