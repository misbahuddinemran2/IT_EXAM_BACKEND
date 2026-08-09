package com.examplatform.modules.liveexam.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LiveExamStartResponse {
    private String sessionId;
    private String examId;
    private String examName;
    private int durationMinutes;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private long remainingSeconds;
    private BigDecimal negativeMarking;
    private LocalDate examDate;   // NEW - exam window date
    private LocalTime endTime;    // NEW - exam window end time (actual, not hardcoded)
    private List<LiveQuestionResponse> questions; // full question set upfront
}
