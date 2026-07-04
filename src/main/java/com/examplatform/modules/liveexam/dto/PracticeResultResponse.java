package com.examplatform.modules.liveexam.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PracticeResultResponse {
    private String examId;
    private String examName;
    private double obtainedMarks;
    private double totalMarks;
    private double percentage;
    private int correctCount;
    private int wrongCount;
    private int skipCount;
    private Integer hypotheticalRank;   // real leaderboard-এর সাথে তুলনা করে
    private int totalParticipants;      // real attempted user সংখ্যা
    private List<LiveExamResultResponse.QuestionResultDto> questions;
}
