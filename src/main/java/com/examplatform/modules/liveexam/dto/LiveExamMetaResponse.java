package com.examplatform.modules.liveexam.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LiveExamMetaResponse {
    private String examId;
    private String examName;
    private List<String> subjectNames;
    private List<String> chapterNames;
    private List<String> topicNames;
}

