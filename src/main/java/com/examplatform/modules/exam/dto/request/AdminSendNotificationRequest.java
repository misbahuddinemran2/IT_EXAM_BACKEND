package com.examplatform.modules.exam.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSendNotificationRequest {
    private NotificationTargetType targetType;
    private String educationLevel; // targetType = EDUCATION_LEVEL হলে দরকার (CLASS_9/NEW_CLASS_10/SSC/HSC_1ST_YEAR/HSC_2ND_YEAR)
    private String examId;         // targetType = WRITTEN_EXAM বা MCQ_EXAM হলে দরকার
    private String title;
    private String body;
    private String deleteAt; // ISO date string, optional, e.g. "2026-07-20T00:00:00"
}
