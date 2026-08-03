package com.examplatform.modules.evaluation.entity;

import com.examplatform.modules.auth.entity.AdminUser;
import com.examplatform.modules.evaluation.enums.EvaluationRunStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "evaluation_run")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationRun {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private EvaluationDataset dataset;

    @Column(name = "dataset_version", length = 50)
    private String datasetVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private EvaluationProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private EvaluationPrompt prompt;

    // পুরো experiment settings-এর immutable snapshot — profile/prompt পরে
    // বদলে গেলেও এই run হুবহু reproduce করার জন্য
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuration_snapshot", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> configurationSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EvaluationRunStatus status = EvaluationRunStatus.PENDING;

    @Column(name = "total_questions", nullable = false)
    @Builder.Default
    private Integer totalQuestions = 0;

    @Column(name = "processed_questions", nullable = false)
    @Builder.Default
    private Integer processedQuestions = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_admin_id")
    private AdminUser triggeredByAdmin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
