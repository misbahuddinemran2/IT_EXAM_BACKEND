package com.examplatform.modules.exam.controller;

import com.examplatform.modules.exam.dto.LeaderboardResponse;
import com.examplatform.modules.exam.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Leaderboard and Ranking APIs")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    @Operation(
            summary = "Get Leaderboard",
            description = "Retrieve top 100 users with their rankings and statistics"
    )
    @ApiResponse(responseCode = "200", description = "Leaderboard retrieved successfully")
    public ResponseEntity<LeaderboardResponse> getLeaderboard(
            @RequestParam String userId) {

        LeaderboardResponse leaderboard = leaderboardService.getLeaderboard(userId);
        return ResponseEntity.ok(leaderboard);
    }
}