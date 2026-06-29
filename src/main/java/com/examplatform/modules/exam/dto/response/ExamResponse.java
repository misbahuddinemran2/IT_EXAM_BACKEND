package com.examplatform.modules.exam.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResponse {

    private String id;
    private String name;
    private String examCode;
    private String examType;
    private String publishStatus;
    private String description;

    // Marks & Duration
    private int totalQuestions;
    private double totalMarks;
    private double passMarks;
    private double negativeMarking;
    private int durationMinutes;

    // Schedule
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Attempt Control
    private String attemptsAllowed; // "Unlimited" / "1" / "3"
    private Integer maxAttempts;

    // Settings
    private boolean allowReview;
    private boolean shuffleQuestions;
    private boolean shuffleOptions;
    private boolean showResultAfterSubmit;
    private boolean isPremiumOnly;

    // Configs
    private List<ExamSubjectConfigResponse> subjectConfigs;
    private List<ExamTopicConfigResponse> topicConfigs;

    // Stats (admin dashboard এ দেখাবে)
    private long totalAttempts;
    private double avgPercentage;

    // Audit
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}