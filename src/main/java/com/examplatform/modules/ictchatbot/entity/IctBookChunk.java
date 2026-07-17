package com.examplatform.modules.ictchatbot.entity;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ict_book_chunk")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IctBookChunk {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "source_upload_id", length = 36)
    private String sourceUploadId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "writer_name", length = 255, nullable = false)
    private String writerName;

    @Column(name = "subject_id", length = 36)
    private String subjectId;

    @Column(name = "chapter_id", length = 36)
    private String chapterId;

    @Column(name = "topic_id", length = 36)
    private String topicId;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    private PGvector embedding;

    @Column(name = "diagram_url", length = 500)
    private String diagramUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = java.util.UUID.randomUUID().toString();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
