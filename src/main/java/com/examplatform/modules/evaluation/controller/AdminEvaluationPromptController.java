package com.examplatform.modules.evaluation.controller;

import com.examplatform.modules.evaluation.dto.*;
import com.examplatform.modules.evaluation.service.EvaluationPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/research/prompts")
@RequiredArgsConstructor
public class AdminEvaluationPromptController {

    private final EvaluationPromptService promptService;

    @PostMapping
    public PromptResponse create(@RequestBody PromptCreateRequest request) {
        return promptService.create(request);
    }

    @PutMapping("/{id}")
    public PromptResponse update(@PathVariable String id, @RequestBody PromptUpdateRequest request) {
        return promptService.update(id, request);
    }

    @GetMapping
    public List<PromptResponse> getAll() {
        return promptService.listAll();
    }

    @GetMapping("/{id}")
    public PromptResponse getById(@PathVariable String id) {
        return promptService.getById(id);
    }
}
