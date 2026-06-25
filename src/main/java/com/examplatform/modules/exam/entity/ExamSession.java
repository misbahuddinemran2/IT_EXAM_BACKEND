package com.examplatform.modules.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_exam_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSession {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "special_exam_id")
    private String specialExamId;

    @Column(name = "exam_type_id")
    private String examTypeId;

    @Column(name = "topic_id")
    private String topicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "total_questions")
    private int totalQuestions;

    @Column(name = "attempted_count")
    private int attemptedCount;

    @Column(name = "correct_count")
    private int correctCount;

    @Column(name = "wrong_count")
    private int wrongCount;

    @Column(name = "skip_count")
    private int skipCount;

    @Column(name = "score")
    private double score;

    @Column(name = "percentage")
    private double percentage;

    @Column(name = "time_spent_sec")
    private int timeSpentSec;

    @Column(name = "is_passed")
    private boolean isPassed;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "notes")
private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        startedAt = LocalDateTime.now();
        status = Status.IN_PROGRESS;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SessionType {
        MOCK, PRACTICE, TOPIC_WISE, BATTLE, WRITTEN, LIVE, CUSTOM, CHALLENGE
    }

    public enum Status {
        IN_PROGRESS, COMPLETED, ABANDONED, TIMED_OUT
    }
}