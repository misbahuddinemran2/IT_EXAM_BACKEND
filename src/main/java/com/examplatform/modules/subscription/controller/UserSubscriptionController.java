package com.examplatform.modules.subscription.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.infrastructure.security.JwtTokenProvider;
import com.examplatform.modules.subscription.entity.SubscriptionPlan;
import com.examplatform.modules.subscription.entity.UserSubscription;
import com.examplatform.modules.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Tag(name = "User Subscription", description = "ইউজারের নিজের Subscription Status")
@SecurityRequirement(name = "Bearer Authentication")
public class UserSubscriptionController {

    private final SubscriptionService subscriptionService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/my")
    @Operation(summary = "নিজের Subscription/Membership Status দেখা")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMySubscription(HttpServletRequest request) {
        String userId = extractUserId(request);

        SubscriptionPlan.PlanType planType = subscriptionService.getUserPlanType(userId);
        boolean isPaid = subscriptionService.isPaidUser(userId);
        var activeSubOpt = subscriptionService.getActiveSubscription(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("planType", planType.name());
        data.put("isPremium", isPaid);
        data.put("status", activeSubOpt.map(sub -> sub.getStatus().name()).orElse("NONE"));
        data.put("planName", activeSubOpt.map(sub -> sub.getPlan().getName()).orElse("Free"));
        data.put("expiresAt", activeSubOpt.map(UserSubscription::getExpiresAt).orElse(null));

        return ResponseEntity.ok(ApiResponse.success("Subscription status পাওয়া গেছে", data));
    }

    private String extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtTokenProvider.getUsernameFromToken(token);
    }
}
