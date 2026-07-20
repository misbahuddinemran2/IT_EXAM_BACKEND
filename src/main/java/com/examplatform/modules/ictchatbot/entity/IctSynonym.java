package com.examplatform.modules.ictchatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ict_synonym")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IctSynonym {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "word", nullable = false, length = 150)
    private String word;

    @Column(name = "canonical_word", nullable = false, length = 150)
    private String canonicalWord;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
