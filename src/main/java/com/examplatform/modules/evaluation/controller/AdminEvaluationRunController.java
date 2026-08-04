package com.examplatform.modules.evaluation.controller;

import com.examplatform.modules.auth.entity.AdminUser;
import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.evaluation.dto.RunComparisonRequest;
import com.examplatform.modules.evaluation.dto.RunComparisonResponse;
import com.examplatform.modules.evaluation.dto.RunMetricSummaryResponse;
import com.examplatform.modules.evaluation.dto.RunResponse;
import com.examplatform.modules.evaluation.dto.RunSummaryResponse;
import com.examplatform.modules.evaluation.dto.RunTriggerRequest;
import com.examplatform.modules.evaluation.engine.MetricsEngineService;
import com.examplatform.modules.evaluation.engine.MetricsReportService;
import com.examplatform.modules.evaluation.service.EvaluationRunService;
import com.examplatform.modules.evaluation.service.EvaluationRunnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin/research/runs")
@RequiredArgsConstructor
public class AdminEvaluationRunController {

    private final EvaluationRunService runService;
    private final EvaluationRunnerService runnerService;
    private final MetricsEngineService metricsEngineService;
    private final MetricsReportService metricsReportService;
    private final AdminUserRepository adminUserRepository;

    /**
     * ধাপ ১ — run তৈরি করে PENDING স্ট্যাটাসে (এখনো execute হয় না)
     */
    @PostMapping
    public RunResponse createRun(@RequestBody RunTriggerRequest request, Authentication auth) {
        return runService.createRun(request, resolveAdminId(auth));
    }

    /**
     * ধাপ ২ — run execute করে (synchronous, blocking)।
     * বড় dataset হলে এই কল সময় নিতে পারে — client-কে সেই অনুযায়ী timeout সেট করতে হবে।
     */
    @PostMapping("/{id}/execute")
    public RunResponse executeRun(@PathVariable String id) {
        runnerService.executeRun(id);
        return runService.getById(id);
    }

    /**
     * ধাপ ৩ — execute শেষ হওয়ার পর মেট্রিক গণনা করে EvaluationResult-এর
     * প্রতিটা row-তে ফিল করে, তারপর run-এর aggregate summary রিটার্ন করে
     */
    @PostMapping("/{id}/compute-metrics")
    public RunMetricSummaryResponse computeMetrics(@PathVariable String id) {
        metricsEngineService.computeMetricsForRun(id);
        return metricsReportService.buildSummary(id);
    }

    @GetMapping("/{id}")
    public RunResponse getById(@PathVariable String id) {
        return runService.getById(id);
    }

    @GetMapping("/{id}/metrics")
    public RunMetricSummaryResponse getMetrics(@PathVariable String id) {
        return metricsReportService.buildSummary(id);
    }

    @GetMapping("/dataset/{datasetId}")
    public List<RunSummaryResponse> getByDataset(@PathVariable String datasetId) {
        return runService.listByDataset(datasetId);
    }

    /**
     * একাধিক run পাশাপাশি তুলনা করার জন্য (Gemini vs Claude vs GPT ইত্যাদি) — শুধু
     * run metadata (status/progress) দেখার জন্য
     * ?ids=run1,run2,run3
     */
    @GetMapping("/compare")
    public List<RunSummaryResponse> compareRuns(@RequestParam List<String> ids) {
        return runService.listByIds(ids);
    }

    /**
     * একাধিক run-এর aggregate মেট্রিক পাশাপাশি তুলনা করার জন্য (research paper
     * টেবিল/গ্রাফের জন্য মূল endpoint)
     */
    @PostMapping("/compare-metrics")
    public RunComparisonResponse compareMetrics(@RequestBody RunComparisonRequest request) {
        return metricsReportService.compareRuns(request.getRunIds());
    }

    private String resolveAdminId(Authentication auth) {
        AdminUser adminUser = adminUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Admin user not found: " + auth.getName()));
        return adminUser.getId();
    }
}
