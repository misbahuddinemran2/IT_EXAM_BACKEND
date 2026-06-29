package com.examplatform.modules.exam.dto.request;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamCreationRequest {

    // ============================================
    // Basic Info
    // ============================================
    private String name;            // required
    private String examCode;        // optional, unique
    private String examType;        // DAILY, WEEKLY, REVISION, SUBJECT_WISE,
    // CHAPTER_WISE, TOPIC_WISE, MIXED, SPECIAL

    private String description;

    // ============================================
    // Marks & Duration
    // ============================================
    private double totalMarks;      // required
    private double passMarks;       // required
    private double negativeMarking; // default 0
    private int durationMinutes;    // required

    // ============================================
    // Schedule
    // ============================================
    private LocalDate examDate;     // required
    private LocalTime startTime;    // required
    private LocalTime endTime;      // required

    // ============================================
    // Attempt Control
    // ============================================
    private Integer maxAttempts;    // NULL = unlimited

    // ============================================
    // Settings
    // ============================================
    private boolean allowReview;
    private boolean shuffleQuestions;
    private boolean shuffleOptions;
    private boolean showResultAfterSubmit;
    private boolean isPremiumOnly;

    // ============================================
    // Question Configuration
    // exam type অনুযায়ী একটা use হবে
    // ============================================

    // SUBJECT_WISE / DAILY / WEEKLY / REVISION →
    // subject level config
    private List<ExamSubjectConfigRequest> subjectConfigs;

    // TOPIC_WISE / CHAPTER_WISE / MIXED →
    // topic level config
    private List<ExamTopicConfigRequest> topicConfigs;

    // Manual question selection (optional)
    // Admin manually question choose করলে
    private List<String> manualQuestionIds;
}