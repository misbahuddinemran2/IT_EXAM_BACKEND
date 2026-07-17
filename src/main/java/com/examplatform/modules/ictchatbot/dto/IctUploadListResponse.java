package com.examplatform.modules.ictchatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IctUploadListResponse {
    private String id;
    private String ocrText;
    private String writerName;
    private String subjectId;
    private String chapterId;
    private String topicId;
    private String status;
    private LocalDateTime createdAt;
}
