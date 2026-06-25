package com.examplatform.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String userId;
    private String fullName;
    private String fullNameBn;
    private String email;
    private String phone;
    private String avatarUrl;
    private String gender;
    private LocalDate dateOfBirth;
    private String district;
    private String educationLevel;
    private String targetExam;
    private String authProvider;
    private boolean isEmailVerified;
    private boolean isPhoneVerified;
    private int loginCount;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}