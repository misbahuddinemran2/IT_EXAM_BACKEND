package com.examplatform.modules.leaderboard.controller;

import com.examplatform.modules.leaderboard.dto.LeaderboardPageResponse;
import com.examplatform.modules.leaderboard.dto.MyRankResponse;
import com.examplatform.modules.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/leaderboard")
@RequiredArgsConstructor
public class OverallLeaderboardController {   // ← নাম পরিবর্তন

    private final LeaderboardService leaderboardService;

    @GetMapping("/overall")
    public LeaderboardPageResponse getOverall(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String userId = auth.getName();
        return leaderboardService.getOverallLeaderboard(userId, page, size);
    }

    @GetMapping("/monthly")
    public LeaderboardPageResponse getMonthly(
            Authentication auth,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String userId = auth.getName();
        return leaderboardService.getMonthlyLeaderboard(userId, yearMonth, page, size);
    }

    @GetMapping("/my-rank")
    public MyRankResponse getMyRank(Authentication auth) {
        String userId = auth.getName();
        return leaderboardService.getMyOverallRank(userId);
    }
}
