package com.examplatform.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String authProvider;
    private boolean isEmailVerified;
    private boolean isPhoneVerified;
}