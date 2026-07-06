package com.examplatform.modules.user.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.common.exception.UnauthorizedException;
import com.examplatform.infrastructure.security.JwtTokenProvider;
import com.examplatform.modules.user.dto.ChangePasswordRequest;
import com.examplatform.modules.user.dto.UpdateProfileRequest;
import com.examplatform.modules.user.dto.UserProfileResponse;
import com.examplatform.modules.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Profile দেখা ও আপডেট")
@SecurityRequirement(name = "Bearer Authentication")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "নিজের Profile দেখা")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            HttpServletRequest request) {
        String userId = extractUserId(request);
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    @PutMapping
    @Operation(summary = "Profile Update")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            HttpServletRequest request,
            @RequestBody UpdateProfileRequest body) {
        String userId = extractUserId(request);
        return ResponseEntity.ok(userProfileService.updateProfile(userId, body));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Password পরিবর্তন")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            HttpServletRequest request,
            @RequestBody ChangePasswordRequest body) {
        String userId = extractUserId(request);
        return ResponseEntity.ok(userProfileService.changePassword(userId, body));
    }

    private String extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization token দিতে হবে");
        }
        String token = authHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Session মেয়াদ শেষ হয়ে গেছে, আবার login করুন");
        }
        try {
            return jwtTokenProvider.getUsernameFromToken(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Session মেয়াদ শেষ হয়ে গেছে, আবার login করুন");
        }
    }
}
