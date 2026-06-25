package com.examplatform.modules.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private String tokenType;
    private long expiresIn;
    private String username;
    private String fullName;
    private String role;
}