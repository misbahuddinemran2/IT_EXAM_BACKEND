package com.examplatform.modules.evaluation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationProfile {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "embedding_model", length = 100)
    private String embeddingModel;

    @Column(name = "top_k", nullable = false)
    @Builder.Default
    private Integer topK = 5;

    @Column(name = "similarity_threshold", precision = 5, scale = 4)
    private BigDecimal similarityThreshold;

    @Column(name = "temperature", precision = 3, scale = 2)
    private BigDecimal temperature;

    @Column(name = "chunk_strategy", length = 100)
    private String chunkStrategy;

    @Column(name = "max_tokens")
    private Integer maxTokens;

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
