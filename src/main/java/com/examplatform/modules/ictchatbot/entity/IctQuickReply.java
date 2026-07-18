package com.examplatform.modules.ictchatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ict_quick_reply")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IctQuickReply {

    @Id
    @Column(length = 36)
    private String id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String keywords;

    @Column(name = "reply_text", columnDefinition = "TEXT", nullable = false)
    private String replyText;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
