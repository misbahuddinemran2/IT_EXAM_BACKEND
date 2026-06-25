package com.examplatform.modules.subscription.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.infrastructure.security.JwtTokenProvider;
import com.examplatform.modules.subscription.service.SubscriptionService;
import com.examplatform.modules.subscription.dto.AdminGrantRequest;
import com.examplatform.modules.subscription.dto.UserSubscriptionResponse;
import com.examplatform.modules.subscription.entity.SubscriptionPlan;
import com.examplatform.modules.subscription.entity.UserSubscription;
import com.examplatform.modules.subscription.repository.UserSubscriptionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Admin Subscription", description = "Admin Subscription Management")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/grant")
    @Operation(summary = "User কে Subscription দেওয়া")
    public ResponseEntity<ApiResponse<Void>> grantSubscription(
            HttpServletRequest request,
            @RequestBody AdminGrantRequest body) {
        String adminId = extractUserId(request);
        subscriptionService.grantSubscription(
                body.getUserId(),
                body.getPlanCode(),
                body.getDurationDays(),
                adminId,
                body.getNotes()
        );
        return ResponseEntity.ok(
            ApiResponse.success("Subscription দেওয়া হয়েছে", null));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "User এর সব Subscription দেখা")
    public ResponseEntity<ApiResponse<List<UserSubscriptionResponse>>> getUserSubscriptions(
            @PathVariable String userId) {
        List<UserSubscription> subs =
                userSubscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<UserSubscriptionResponse> response = subs.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Subscriptions পাওয়া গেছে", response));
    }

    @GetMapping("/plans")
    @Operation(summary = "সব Subscription Plan দেখা")
    public ResponseEntity<ApiResponse<List<SubscriptionPlan>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.success("Plans পাওয়া গেছে",
                subscriptionService.getAllActivePlans()));
    }

    private UserSubscriptionResponse toResponse(UserSubscription sub) {
        return UserSubscriptionResponse.builder()
                .id(sub.getId())
                .userId(sub.getUserId())
                .planName(sub.getPlan().getName())
                .planCode(sub.getPlan().getPlanCode())
                .planType(sub.getPlan().getPlanType().name())
                .status(sub.getStatus().name())
                .paymentMethod(sub.getPaymentMethod().name())
                .startsAt(sub.getStartsAt())
                .expiresAt(sub.getExpiresAt())
                .amountPaid(sub.getAmountPaid())
                .notes(sub.getNotes())
                .build();
    }

    private String extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtTokenProvider.getUsernameFromToken(token);
    }
}