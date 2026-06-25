package com.examplatform.modules.question.controller.admin;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.modules.question.dto.request.AttemptRequest;
import com.examplatform.modules.question.dto.response.QuestionAnalyticsResponse;
import com.examplatform.modules.question.service.QuestionAnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuestionAnalyticsController {

    private final QuestionAnalyticsService analyticsService;

    @PostMapping("/pub/questions/attempt")
    public ResponseEntity<ApiResponse<Void>> recordAttempt(
            @Valid @RequestBody AttemptRequest request) {
        analyticsService.recordAttempt(request);
        return ResponseEntity.ok(
            ApiResponse.success("Attempt recorded", null)
        );
    }

    @GetMapping("/admin/questions/{id}/analytics")
    public ResponseEntity<ApiResponse<QuestionAnalyticsResponse>>
            getAnalytics(@PathVariable String id) {
        return ResponseEntity.ok(
            ApiResponse.success(
                analyticsService.getAnalytics(id)
            )
        );
    }

    @GetMapping("/admin/analytics/hard-questions")
    public ResponseEntity<ApiResponse<
            List<QuestionAnalyticsResponse>>>
            getHardQuestions() {
        return ResponseEntity.ok(
            ApiResponse.success(
                analyticsService.getHardQuestions()
            )
        );
    }

    @GetMapping("/admin/analytics/most-attempted")
    public ResponseEntity<ApiResponse<
            List<QuestionAnalyticsResponse>>>
            getMostAttempted() {
        return ResponseEntity.ok(
            ApiResponse.success(
                analyticsService.getMostAttempted()
            )
        );
    }
}