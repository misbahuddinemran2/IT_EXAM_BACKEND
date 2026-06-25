
package com.examplatform.modules.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserResponse {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String createdAt;
    private String lastLoginAt;
    private boolean isActive;
    private String subscriptionStatus;
    private String subscriptionExpiry;
    private String subscriptionPlanName;
    private long totalExams;
}
