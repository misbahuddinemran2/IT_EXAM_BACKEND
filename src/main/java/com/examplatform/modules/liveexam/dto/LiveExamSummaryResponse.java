package com.examplatform.modules.liveexam.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveExamSummaryResponse {
    private String id;
    private String name;
    private String examCode;
    private String examType;
    private String description;

    private List<String> subjectNames;
    private List<String> chapterNames;
    private List<String> topicNames;

    private int totalQuestions;
    private BigDecimal totalMarks;
    private BigDecimal passMarks;
    private int durationMinutes;

    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private List<String> targetLevels;
    private boolean isPremiumOnly;

    private String attemptStatus;
    private boolean windowEnded;

    // নতুন: ছাত্র আগেই দিয়ে থাকলে তার প্রাপ্ত নম্বর (SUBMITTED/AUTO_SUBMITTED হলে ছাড়া null)
    private BigDecimal obtainedMarks;
}
