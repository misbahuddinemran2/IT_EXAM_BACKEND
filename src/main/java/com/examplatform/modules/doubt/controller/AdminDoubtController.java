package com.examplatform.modules.doubt.controller;

import com.examplatform.modules.doubt.dto.*;
import com.examplatform.modules.doubt.service.AdminDoubtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/doubts")
@RequiredArgsConstructor
public class AdminDoubtController {

    private final AdminDoubtService adminDoubtService;

    @GetMapping("/pending")
    public List<DoubtSummaryResponse> getPending() {
        return adminDoubtService.getByStatus("PENDING");
    }

    @GetMapping("/reviewed")
    public List<DoubtSummaryResponse> getReviewed() {
        return adminDoubtService.getByStatus("REVIEWED");
    }

    @GetMapping("/answered")
    public List<DoubtSummaryResponse> getAnswered() {
        return adminDoubtService.getByStatus("ANSWERED");
    }

    @PostMapping("/{id}/accept")
    public DoubtResponse accept(@PathVariable String id) {
        return adminDoubtService.acceptDoubt(id);
    }

    @PostMapping("/{id}/generate-ai")
    public AiGenerateResponse generateAi(@PathVariable String id) {
        return adminDoubtService.generateAiPreview(id);
    }

    @PostMapping("/{id}/answer")
    public DoubtResponse saveAnswer(
            @PathVariable String id,
            @RequestBody AdminAnswerRequest request) {
        return adminDoubtService.saveAnswer(id, request);
    }
}
