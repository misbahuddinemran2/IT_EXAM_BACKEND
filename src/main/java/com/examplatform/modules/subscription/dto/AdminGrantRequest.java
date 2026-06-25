package com.examplatform.modules.subscription.dto;

import lombok.Data;

@Data
public class AdminGrantRequest {
    private String userId;
    private String planCode;    // MONTHLY, YEARLY, CUSTOM
    private int durationDays;
    private String notes;
}