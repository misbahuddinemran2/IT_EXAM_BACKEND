package com.examplatform.modules.ictchatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ict_query_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IctQueryLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "response_path", nullable = false, length = 20)
    private String responsePath;

    @Column(name = "answer_found", nullable = false)
    @Builder.Default
    private Boolean answerFound = false;

    @Column(name = "matched_writer_names", columnDefinition = "TEXT")
    private String matchedWriterNames;

    @Column(name = "closest_chunk_distance")
    private Double closestChunkDistance;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "quick_reply_match_type", length = 20)
    private String quickReplyMatchType;

    @Column(name = "quick_reply_match_score")
    private Double quickReplyMatchScore;

    @Column(name = "quick_reply_matched_keyword", columnDefinition = "TEXT")
    private String quickReplyMatchedKeyword;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
