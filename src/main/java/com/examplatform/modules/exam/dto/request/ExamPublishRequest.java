package com.examplatform.modules.exam.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamPublishRequest {

    // Optional — publish করার আগে
    // last minute date/time update করতে চাইলে
    private String examDate;
    private String startTime;
    private String endTime;
}