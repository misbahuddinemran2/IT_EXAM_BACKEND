package com.examplatform.modules.exam.controller;

import com.examplatform.modules.exam.dto.AIExplanationRequest;
import com.examplatform.modules.exam.dto.AIExplanationResponse;
import com.examplatform.modules.exam.service.GeminiExplanationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Explanations", description = "AI-powered Question Explanations using Google Gemini")
public class AIExplanationController {

    private final GeminiExplanationService geminiExplanationService;

    @PostMapping("/explain-answer")
    @Operation(
            summary = "Explain Answer",
            description = "Get AI-powered explanation for a question using Google Gemini API"
    )
    @ApiResponse(responseCode = "200", description = "Explanation generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<AIExplanationResponse> explainAnswer(
            @RequestBody AIExplanationRequest request) {

        AIExplanationResponse response = geminiExplanationService.generateExplanation(request);
        return ResponseEntity.ok(response);
    }
}