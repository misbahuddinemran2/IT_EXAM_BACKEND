package com.examplatform.modules.exam.controller;

import com.examplatform.modules.exam.dto.AnalyticsResponse;
import com.examplatform.modules.exam.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "User Performance Analytics APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get User Analytics",
            description = "Retrieve detailed performance analytics including subject-wise, monthly trends, and weak topics"
    )
    @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @PathVariable String userId) {

        AnalyticsResponse analytics = analyticsService.getAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }
}