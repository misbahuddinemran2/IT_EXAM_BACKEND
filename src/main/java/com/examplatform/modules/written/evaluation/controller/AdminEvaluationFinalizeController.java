package com.examplatform.modules.written.evaluation.controller;

import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.written.evaluation.manual.request.ManualEvaluationRequest;
import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.evaluation.service.WrittenEvaluationFinalizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/**
 * Finalizes an evaluation for ANY evaluation mode (MANUAL, AI, or HYBRID).
 * Admin reviews predicted marks (if any) shown in GET /written/evaluations/submission/{id},
 * edits them as needed, and submits the final per-part marks here.
 */
@RestController
@RequestMapping("/admin/written/evaluations/finalize")
@RequiredArgsConstructor
public class AdminEvaluationFinalizeController {

    private final WrittenEvaluationFinalizeService finalizeService;
    private final AdminUserRepository adminUserRepository;

    @PostMapping("/{submissionId}")
    public EvaluationResponse finalizeEvaluation(
            @PathVariable String submissionId,
            @Valid @RequestBody ManualEvaluationRequest request,
            Authentication auth) {
        String adminId = resolveAdminId(auth);
        return finalizeService.finalizeEvaluation(submissionId, request, adminId);
    }

    private String resolveAdminId(Authentication auth) {
        return adminUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Admin user not found: " + auth.getName()))
                .getId();
    }
}
