package com.examplatform.modules.question.entity;

import com.examplatform.modules.examtype.entity.ExamType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_exam_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionExamType {

    @EmbeddedId
    private QuestionExamTypeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("examTypeId")
    @JoinColumn(name = "exam_type_id")
    private ExamType examType;

    @Column(name = "relevance_score")
    @Builder.Default
    private double relevanceScore = 1.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class QuestionExamTypeId
            implements java.io.Serializable {
        @Column(name = "question_id")
        private String questionId;

        @Column(name = "exam_type_id")
        private String examTypeId;
    }
}