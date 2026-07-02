package com.examplatform.modules.question.entity;

import com.examplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_upload_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUploadJob extends BaseEntity {

    @Column(name = "uploaded_by", nullable = false, length = 36)
    private String uploadedBy;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size_kb", nullable = false)
    private int fileSizeKb;

    @Column(name = "total_rows", nullable = false)
    @Builder.Default
    private int totalRows = 0;

    @Column(name = "valid_rows", nullable = false)
    @Builder.Default
    private int validRows = 0;

    @Column(name = "failed_rows", nullable = false)
    @Builder.Default
    private int failedRows = 0;

    @Column(name = "imported_rows", nullable = false)
    @Builder.Default
    private int importedRows = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private JobStatus status = JobStatus.UPLOADED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "error_report", columnDefinition = "jsonb")
    private String errorReport;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum JobStatus {
        UPLOADED, VALIDATING, VALIDATION_DONE,
        IMPORTING, COMPLETED, FAILED
    }
}
