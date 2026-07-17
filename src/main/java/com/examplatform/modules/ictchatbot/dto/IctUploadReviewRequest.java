package com.examplatform.modules.ictchatbot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IctUploadReviewRequest {
    private String ocrText;      // admin এর ঠিক করা টেক্সট
    private String writerName;
    private String subjectId;
    private String chapterId;
    private String topicId;
    private String reviewedByAdminId;
}
