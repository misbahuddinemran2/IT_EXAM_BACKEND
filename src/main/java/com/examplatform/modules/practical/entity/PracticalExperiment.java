package com.examplatform.modules.practical.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "practical_experiments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticalExperiment {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "chapter_id", nullable = false, length = 36)
    private String chapterId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "title_bn")
    private String titleBn;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = false;

    // কোন HSC session(s) এই experiment দেখতে পারবে ("ALL" = সবাই)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_sessions", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<String> targetSessions = List.of("ALL");

    @Column(name = "order_number", nullable = false)
    @Builder.Default
    private int orderNumber = 0;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
