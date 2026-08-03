package com.examplatform.modules.evaluation.entity;

import com.examplatform.modules.evaluation.enums.EvaluationResultStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "evaluation_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResult {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private EvaluationRun run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private EvaluationQuestion question;

    // ===== IctAskService.ask() থেকে reuse করা raw output =====
    @Column(name = "generated_answer", columnDefinition = "TEXT")
    private String generatedAnswer;

    @Column(name = "response_path", length = 20)
    private String responsePath;

    @Column(name = "matched_writer_names", columnDefinition = "TEXT")
    private String matchedWriterNames;

    @Column(name = "answer_found", nullable = false)
    @Builder.Default
    private boolean answerFound = false;

    @Column(name = "from_cache", nullable = false)
    @Builder.Default
    private boolean fromCache = false;

    // ===== Retrieval metadata (IctBookChunkRepository থেকে আলাদাভাবে capture) =====
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "retrieved_chunk_ids", columnDefinition = "jsonb")
    private List<String> retrievedChunkIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "retrieved_chunk_distances", columnDefinition = "jsonb")
    private List<Double> retrievedChunkDistances;

    @Column(name = "closest_chunk_distance")
    private Double closestChunkDistance;

    @Column(name = "retrieved_chunk_count")
    private Integer retrievedChunkCount;

    @Column(name = "candidate_chunk_count")
    private Integer candidateChunkCount;

    // ===== Performance metric =====
    @Column(name = "retrieval_latency_ms")
    private Integer retrievalLatencyMs;

    @Column(name = "llm_latency_ms")
    private Integer llmLatencyMs;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    // ===== Reporting-এর জন্য denormalized snapshot fields =====
    @Column(name = "prompt_version", length = 50)
    private String promptVersion;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "token_input")
    private Integer tokenInput;

    @Column(name = "token_output")
    private Integer tokenOutput;

    // ===== Answer-quality metric (post-processing এ আপডেট হবে) =====
    @Column(name = "exact_match")
    private Boolean exactMatch;

    @Column(name = "semantic_similarity_score", precision = 5, scale = 4)
    private BigDecimal semanticSimilarityScore;

    @Column(name = "token_f1_score", precision = 5, scale = 4)
    private BigDecimal tokenF1Score;

    // ===== Citation metric =====
    @Column(name = "citation_coverage", precision = 5, scale = 4)
    private BigDecimal citationCoverage;

    @Column(name = "citation_precision", precision = 5, scale = 4)
    private BigDecimal citationPrecision;

    @Column(name = "citation_recall", precision = 5, scale = 4)
    private BigDecimal citationRecall;

    @Column(name = "citation_faithfulness", precision = 5, scale = 4)
    private BigDecimal citationFaithfulness;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EvaluationResultStatus status = EvaluationResultStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

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
