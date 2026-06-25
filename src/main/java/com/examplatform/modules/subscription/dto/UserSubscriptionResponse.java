package com.examplatform.modules.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionResponse {
    private String id;
    private String userId;
    private String planName;
    private String planCode;
    private String planType;
    private String status;
    private String paymentMethod;
    private LocalDateTime startsAt;
    private LocalDateTime expiresAt;
    private double amountPaid;
    private String notes;
}