package com.examplatform.modules.liveexam.dto;

import lombok.*;
import java.time.LocalDateTime;
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
    private List<LiveQuestionResponse> questions; // full question set upfront
}
