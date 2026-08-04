package com.examplatform.modules.evaluation.controller;

import com.examplatform.modules.evaluation.dto.*;
import com.examplatform.modules.evaluation.service.EvaluationProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/research/profiles")
@RequiredArgsConstructor
public class AdminEvaluationProfileController {

    private final EvaluationProfileService profileService;

    @PostMapping
    public ProfileResponse create(@RequestBody ProfileCreateRequest request) {
        return profileService.create(request);
    }

    @PutMapping("/{id}")
    public ProfileResponse update(@PathVariable String id, @RequestBody ProfileUpdateRequest request) {
        return profileService.update(id, request);
    }

    @GetMapping
    public List<ProfileResponse> getAll() {
        return profileService.listAll();
    }

    @GetMapping("/{id}")
    public ProfileResponse getById(@PathVariable String id) {
        return profileService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        profileService.delete(id);
    }
}
