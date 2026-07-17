package com.examplatform.modules.ictchatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDateTime;

@Entity
@Table(name = "ict_answer_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IctAnswerCache {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "question_embedding", columnDefinition = "vector(768)", nullable = false)
    @ColumnTransformer(write = "?::vector")
    private String questionEmbedding;

    @Column(name = "cached_answer", columnDefinition = "TEXT", nullable = false)
    private String cachedAnswer;

    @Column(name = "hit_count", nullable = false)
    private Integer hitCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = java.util.UUID.randomUUID().toString();
        if (this.hitCount == null) this.hitCount = 1;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
