package com.examplatform.modules.user.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.modules.user.dto.AuthResponse;
import com.examplatform.modules.user.dto.LoginRequest;
import com.examplatform.modules.user.dto.RegisterRequest;
import com.examplatform.modules.user.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
@Tag(name = "User Auth", description = "User Registration & Login")
public class UserAuthController {

    private final UserAuthService userAuthService;

    @PostMapping("/register")
    @Operation(summary = "নতুন User Registration")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userAuthService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "User Login — Email অথবা Phone দিয়ে")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userAuthService.login(request));
    }
}