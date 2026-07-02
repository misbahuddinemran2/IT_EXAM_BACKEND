package com.examplatform.modules.liveexam.dto;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LeaderboardEntryResponse {
    private int rank;
    private String userId;
    private String userName; // service এ join করে বসাবে
    private double obtainedMarks;
    private double totalMarks;
    private double percentage;
    private boolean isCurrentUser;
}
