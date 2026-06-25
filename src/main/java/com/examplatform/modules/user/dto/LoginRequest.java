package com.examplatform.modules.user.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String identifier;  // email অথবা phone
    private String password;
}