package com.examplatform.modules.exam.controller;

import com.examplatform.modules.exam.dto.DashboardResponse;
import com.examplatform.modules.exam.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "User Dashboard APIs")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get User Dashboard",
            description = "Retrieve complete dashboard data including stats, recent exams, and weak areas"
    )
    @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<DashboardResponse> getDashboard(
            @PathVariable String userId,
            @RequestParam String userName) {

        DashboardResponse dashboard = dashboardService.getDashboard(userId, userName);
        return ResponseEntity.ok(dashboard);
    }
}