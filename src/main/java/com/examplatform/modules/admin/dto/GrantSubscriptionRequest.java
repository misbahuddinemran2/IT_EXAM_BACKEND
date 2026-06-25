


package com.examplatform.modules.admin.dto;

import lombok.Data;

@Data
public class GrantSubscriptionRequest {
    private String planId;
    private int durationDays;
    private String notes;
}
