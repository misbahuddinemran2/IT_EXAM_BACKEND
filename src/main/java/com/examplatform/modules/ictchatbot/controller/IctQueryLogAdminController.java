package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.dto.IctQueryLogStatsResponse;
import com.examplatform.modules.ictchatbot.entity.IctQuerySummary;
import com.examplatform.modules.ictchatbot.service.IctQueryLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    // আগে শুধু summarize করুন, raw log এখনো মুছবে না — review করার জন্য নিরাপদ ধাপ
    @PostMapping("/summarize")
    public Map<String, Object> summarizeOnly() {
        int count = queryLogService.summarizeOnly();
        return Map.of("summaryRowsCreated", count);
    }

    // Raw log সম্পূর্ণ মুছে ফেলা (summarize করার পরে, ম্যানুয়ালি কল করবেন)
    @PostMapping("/cleanup")
    public Map<String, Object> cleanup() {
        long deletedCount = queryLogService.cleanupRawLog();
        return Map.of("rawLogRowsDeleted", deletedCount);
    }

    // সুবিধার জন্য — একসাথে summarize + cleanup
    @PostMapping("/summarize-and-cleanup")
    public Map<String, Object> summarizeAndCleanup() {
        int count = queryLogService.summarizeAndCleanup();
        return Map.of("summaryRowsCreated", count);
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
}
