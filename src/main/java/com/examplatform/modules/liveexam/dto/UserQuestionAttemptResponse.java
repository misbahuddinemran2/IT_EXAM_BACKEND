package com.examplatform.modules.liveexam.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuestionAttemptResponse {
    private String questionId;
    private String examId;
    private String sessionId;
    private String selectedOptionId;
    private boolean isCorrect;
    private boolean isSkipped;
    private LocalDateTime answeredAt;
}
