package com.examplatform.modules.evaluation.controller;

import com.examplatform.modules.evaluation.dto.ResultResponse;
import com.examplatform.modules.evaluation.dto.ResultSummaryResponse;
import com.examplatform.modules.evaluation.service.EvaluationResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/research/results")
@RequiredArgsConstructor
public class AdminEvaluationResultController {

    private final EvaluationResultService resultService;

    @GetMapping("/{id}")
    public ResultResponse getById(@PathVariable String id) {
        return resultService.getById(id);
    }

    @GetMapping("/run/{runId}")
    public Page<ResultSummaryResponse> getByRun(
            @PathVariable String runId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return resultService.listByRun(runId, PageRequest.of(page, size));
    }
}
