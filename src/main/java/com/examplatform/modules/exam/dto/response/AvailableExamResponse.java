package com.examplatform.modules.exam.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableExamResponse {

    private String id;
    private String name;
    private String examType;
    private String description;

    // Exam details
    private int totalQuestions;
    private double totalMarks;
    private double passMarks;
    private double negativeMarking;
    private int durationMinutes;

    // Schedule
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Attempt info (user specific)
    private String attemptsAllowed;     // "Unlimited" / "1" / "3"
    private long attemptsUsed;          // user কতবার দিয়েছে
    private Long attemptsRemaining;     // NULL = unlimited
    private boolean canAttempt;         // দিতে পারবে কিনা

    // Exam status (user এর জন্য)
    private String examStatus;
    // NOT_YET_AVAILABLE / AVAILABLE / EXPIRED

    // User এর আগের result (যদি থাকে)
    private Double bestPercentage;      // আগে দিলে best score
    private boolean hasPassed;

    // Access
    private boolean isPremiumOnly;
}