package com.examplatform.modules.written.evaluation.controller;

import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.evaluation.service.WrittenEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/written/evaluations")
@RequiredArgsConstructor
public class WrittenEvaluationController {

    private final WrittenEvaluationService evaluationService;

    // Student views their own evaluation result for a submission
    @GetMapping("/submission/{submissionId}")
    public EvaluationResponse getMyEvaluation(@PathVariable String submissionId) {
        return evaluationService.getEvaluationBySubmissionId(submissionId);
    }
}