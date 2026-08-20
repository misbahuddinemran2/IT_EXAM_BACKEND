package com.examplatform.modules.challenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChallengeDetailResponse {
    private String id;
    private String mode;
    private String status;
    private String creatorId;
    private String creatorName;
    private String opponentId;
    private String opponentName;
    private String chapterId;
    private String topicId;
    private int questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @JsonProperty("isMeCreator")
    private boolean isMeCreator;

    @JsonProperty("hasIStarted")
    private boolean hasIStarted;

    @JsonProperty("hasOpponentFinished")
    private boolean hasOpponentFinished;

    private List<ChallengeQuestionResponse> questions;
}
