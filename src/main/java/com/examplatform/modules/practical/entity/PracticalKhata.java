package com.examplatform.modules.practical.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "practical_khata")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticalKhata {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "experiment_id", nullable = false, length = 36, unique = true)
    private String experimentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "khata_type", nullable = false, length = 10)
    @Builder.Default
    private KhataType khataType = KhataType.TEXT;

    @Column(name = "pdf_url", columnDefinition = "TEXT")
    private String pdfUrl;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum KhataType { PDF, TEXT, BOTH }
}
