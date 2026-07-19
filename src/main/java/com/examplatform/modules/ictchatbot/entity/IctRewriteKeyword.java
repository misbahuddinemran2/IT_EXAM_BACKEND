package com.examplatform.modules.ictchatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ict_rewrite_keyword")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IctRewriteKeyword {

    @Id
    @GeneratedValue
    private java.util.UUID id;

    @Column(name = "category", nullable = false, length = 30)
    private String category;

    @Column(name = "keywords", nullable = false, columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

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
