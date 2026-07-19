package com.examplatform.modules.ictchatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ict_query_summary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IctQuerySummary {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "ask_count", nullable = false)
    private Integer askCount;

    @Column(name = "not_found_count", nullable = false)
    private Integer notFoundCount;

    @Column(name = "quick_reply_count", nullable = false)
    private Integer quickReplyCount;

    @Column(name = "cache_hit_count", nullable = false)
    private Integer cacheHitCount;

    @Column(name = "gemini_generated_count", nullable = false)
    private Integer geminiGeneratedCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
