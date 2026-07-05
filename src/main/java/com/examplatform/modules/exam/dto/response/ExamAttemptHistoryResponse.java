package com.examplatform.modules.exam.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAttemptHistoryResponse {

    private String id;
    private String examId;
    private String examName;
    private String examType;
    private String sessionId;

    private int attemptNumber;
    private double obtainedMarks;
    private double totalMarks;
    private double percentage;
    private boolean isPassed;
    private boolean resultPublished;
    

    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    
    private String examEndTime;

    private List<String> subjectNames;
    private List<String> chapterNames;
    private List<String> topicNames;
    private LocalDateTime startedAt;
    private int timeTakenSeconds;

}
