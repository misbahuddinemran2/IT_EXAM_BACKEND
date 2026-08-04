package com.examplatform.modules.evaluation.entity;

import com.examplatform.modules.evaluation.enums.EvaluationQuestionType;
import com.examplatform.modules.evaluation.enums.QuestionDifficulty;
import com.examplatform.modules.ictchatbot.entity.IctBookChunk;
import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationQuestion {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private EvaluationDataset dataset;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "expected_answer", columnDefinition = "TEXT", nullable = false)
    private String expectedAnswer;

    @Column(name = "expected_writer_names", length = 500)
    private String expectedWriterNames;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 20)
    private QuestionDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", length = 30)
    private EvaluationQuestionType questionType;

    @Column(name = "reference_book", length = 255)
    private String referenceBook;

    @Column(name = "reference_page")
    private Integer referencePage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_chunk_id")
    private IctBookChunk referenceChunk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
