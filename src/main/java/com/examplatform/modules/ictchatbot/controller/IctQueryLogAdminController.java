package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.dto.IctQueryLogStatsResponse;
import com.examplatform.modules.ictchatbot.entity.IctQuerySummary;
import com.examplatform.modules.ictchatbot.service.IctQueryLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/ict/query-log")
@RequiredArgsConstructor
public class IctQueryLogAdminController {

    private final IctQueryLogService queryLogService;

    @GetMapping("/stats")
    public IctQueryLogStatsResponse getStats() {
        return IctQueryLogStatsResponse.builder()
                .rawLogCount(queryLogService.getRawLogCount())
                .build();
    }

    @PostMapping("/summarize")
    public Map<String, Object> summarizeOnly() {
        int count = queryLogService.summarizeOnly();
        return Map.of("questionsProcessed", count);
    }

    @PostMapping("/cleanup")
    public Map<String, Object> cleanup() {
        long deletedCount = queryLogService.cleanupRawLog();
        return Map.of("rawLogRowsDeleted", deletedCount);
    }

    @PostMapping("/summarize-and-cleanup")
    public Map<String, Object> summarizeAndCleanup() {
        int count = queryLogService.summarizeAndCleanup();
        return Map.of("questionsProcessed", count);
    }

    @GetMapping("/top-gemini-calls")
    public List<IctQuerySummary> getTopByGeminiCalls() {
        return queryLogService.getTopByGeminiCalls();
    }

    @GetMapping("/top-not-found")
    public List<IctQuerySummary> getTopByNotFound() {
        return queryLogService.getTopByNotFound();
    }

    @GetMapping("/summaries")
    public List<IctQuerySummary> getAllSummaries() {
        return queryLogService.getAllSummaries();
    }

    // একটা নির্দিষ্ট summary entry মুছে ফেলা
    @DeleteMapping("/summaries/{id}")
    public Map<String, Object> deleteSummary(@PathVariable UUID id) {
        queryLogService.deleteSummary(id);
        return Map.of("deleted", true);
    }

    // একাধিক (mark করা) summary entry একসাথে মুছে ফেলা
    @PostMapping("/summaries/bulk-delete")
    public Map<String, Object> bulkDeleteSummaries(@RequestBody List<UUID> ids) {
        int count = queryLogService.deleteSummaries(ids);
        return Map.of("deletedCount", count);
    }
}
