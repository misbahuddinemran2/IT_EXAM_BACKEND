package com.examplatform.modules.written.evaluation.manual.controller;

import com.examplatform.modules.written.evaluation.manual.request.ManualEvaluationRequest;
import com.examplatform.modules.written.evaluation.manual.service.WrittenManualEvaluationService;
import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/written/evaluations/manual")
@RequiredArgsConstructor
public class AdminManualEvaluationController {

    private final WrittenManualEvaluationService manualEvaluationService;

    @PostMapping("/{submissionId}")
    public EvaluationResponse submitManualEvaluation(
            @PathVariable String submissionId,
            @Valid @RequestBody ManualEvaluationRequest request,
            Authentication auth) {
        return manualEvaluationService.submitManualEvaluation(submissionId, request, auth.getName());
    }
}