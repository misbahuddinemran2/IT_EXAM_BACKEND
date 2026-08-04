package com.examplatform.modules.evaluation.controller;

import com.examplatform.modules.evaluation.dto.*;
import com.examplatform.modules.evaluation.service.EvaluationQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/research/questions")
@RequiredArgsConstructor
public class AdminEvaluationQuestionController {

    private final EvaluationQuestionService questionService;

    @PostMapping
    public QuestionResponse create(@RequestBody QuestionCreateRequest request) {
        return questionService.create(request);
    }

    @PostMapping("/bulk-upload")
    public List<QuestionResponse> bulkUpload(@RequestBody QuestionBulkUploadRequest request) {
        return questionService.bulkCreate(request);
    }

    @PutMapping("/{id}")
    public QuestionResponse update(@PathVariable String id, @RequestBody QuestionUpdateRequest request) {
        return questionService.update(id, request);
    }

    @GetMapping("/{id}")
    public QuestionResponse getById(@PathVariable String id) {
        return questionService.getById(id);
    }

    @GetMapping("/dataset/{datasetId}")
    public List<QuestionResponse> getByDataset(@PathVariable String datasetId) {
        return questionService.listByDataset(datasetId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        questionService.delete(id);
    }
}
