package com.examplatform.modules.question.entity;

import com.examplatform.common.entity.BaseEntity;
import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends BaseEntity {

   @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
private String questionText;

@Column(name = "question_text_bn", columnDefinition = "TEXT")
private String questionTextBn;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    @Builder.Default
    private QuestionType questionType = QuestionType.MCQ_SINGLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 10)
    @Builder.Default
    private Language language = Language.EN;

    // Knowledge Hierarchy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // Quality
    @Column(name = "difficulty_level", nullable = false)
    @Builder.Default
    private int difficultyLevel = 3;

    @Enumerated(EnumType.STRING)
    @Column(name = "cognitive_level", nullable = false, length = 20)
    @Builder.Default
    private CognitiveLevel cognitiveLevel = CognitiveLevel.REMEMBER;

    @Column(name = "estimated_time_sec", nullable = false)
    @Builder.Default
    private int estimatedTimeSec = 60;

    // Source
    @Column(name = "source_reference", length = 500)
    private String sourceReference;

    @Column(name = "year_appeared")
    private Integer yearAppeared;

    @Column(name = "is_reusable", nullable = false)
    @Builder.Default
    private boolean isReusable = true;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private QuestionStatus status = QuestionStatus.DRAFT;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "reported_count", nullable = false)
    @Builder.Default
    private int reportedCount = 0;

    // Duplicate detection
    @Column(name = "content_hash", length = 64, unique = true)
    private String contentHash;

    // AI fields
    @Column(name = "ai_generated", nullable = false)
    @Builder.Default
    private boolean aiGenerated = false;

    @Column(name = "ai_confidence_score")
    private Double aiConfidenceScore;

    @Column(name = "embedding_vector", columnDefinition = "TEXT")
    private String embeddingVector;

    // Versioning
    @Column(name = "version", nullable = false)
    @Builder.Default
    private int version = 1;

    // Audit
    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;

    public enum QuestionType {
        MCQ_SINGLE, MCQ_MULTI, TRUE_FALSE
    }

    public enum Language {
        EN, BN, BOTH
    }

    public enum CognitiveLevel {
        REMEMBER, UNDERSTAND, APPLY, ANALYZE, EVALUATE
    }

    public enum QuestionStatus {
        DRAFT, UNDER_REVIEW, APPROVED, REJECTED, ARCHIVED
    }
}