package com.examplatform.modules.leaderboard.controller;

import com.examplatform.modules.leaderboard.dto.LeaderboardSettingsUpdateRequest;
import com.examplatform.modules.leaderboard.entity.LeaderboardSettings;
import com.examplatform.modules.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/leaderboard")
@RequiredArgsConstructor
public class AdminLeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/settings")
    public LeaderboardSettings getSettings() {
        return leaderboardService.getSettings();
    }

    @PutMapping("/settings")
    public LeaderboardSettings updateSettings(
            @RequestBody LeaderboardSettingsUpdateRequest req,
            Authentication auth) {
        String adminId = auth.getName();
        return leaderboardService.updateSettings(req, adminId);
    }
}
