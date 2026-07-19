package com.examplatform.modules.ictchatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ict_rewrite_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IctRewriteCache {

    @Id
    @GeneratedValue
    private java.util.UUID id;

    @Column(name = "original_answer_hash", nullable = false, length = 64)
    private String originalAnswerHash;

    @Column(name = "category", nullable = false, length = 30)
    private String category;

    @Column(name = "rewritten_answer", nullable = false, columnDefinition = "TEXT")
    private String rewrittenAnswer;

    @Column(name = "hit_count", nullable = false)
    @Builder.Default
    private Integer hitCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
