package com.examplatform.modules.admin.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.modules.admin.dto.AdminStatsResponse;
import com.examplatform.modules.admin.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(
            ApiResponse.success("Stats fetched", adminStatsService.getStats())
        );
    }
}