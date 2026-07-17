package com.examplatform.modules.ictchatbot.entity;

import com.examplatform.modules.ictchatbot.enums.IctUploadStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ict_ocr_upload")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IctOcrUpload {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "ocr_text", columnDefinition = "TEXT", nullable = false)
    private String ocrText;

    @Column(name = "writer_name", length = 255)
    private String writerName;

    @Column(name = "subject_id", length = 36)
    private String subjectId;

    @Column(name = "chapter_id", length = 36)
    private String chapterId;

    @Column(name = "topic_id", length = 36)
    private String topicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IctUploadStatus status;

    @Column(name = "reviewed_by_admin_id", length = 36)
    private String reviewedByAdminId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = java.util.UUID.randomUUID().toString();
        if (this.status == null) this.status = IctUploadStatus.PENDING;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
