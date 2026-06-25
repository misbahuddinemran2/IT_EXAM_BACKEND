package com.examplatform.modules.user.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String fullNameBn;
    private String email;       // email অথবা phone — একটা লাগবেই
    private String phone;
    private String password;
    private String referredBy;  // optional
}