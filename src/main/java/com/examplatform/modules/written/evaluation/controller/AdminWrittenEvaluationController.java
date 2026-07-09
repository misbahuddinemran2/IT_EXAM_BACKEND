package com.examplatform.modules.written.evaluation.controller;

import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.evaluation.service.WrittenEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/written/evaluations")
@RequiredArgsConstructor
public class AdminWrittenEvaluationController {

    private final WrittenEvaluationService evaluationService;

    @GetMapping("/{evaluationId}")
    public EvaluationResponse getEvaluation(@PathVariable String evaluationId) {
        return evaluationService.getEvaluationById(evaluationId);
    }

    @GetMapping("/exam/{examId}")
    public List<EvaluationResponse> getEvaluationsForExam(@PathVariable String examId) {
        return evaluationService.getEvaluationsForExam(examId);
    }
}