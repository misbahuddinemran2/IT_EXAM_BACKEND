package com.examplatform.appconfig.controller;

import com.examplatform.appconfig.dto.OnboardingSlideRequest;
import com.examplatform.appconfig.dto.SlideReorderRequest;
import com.examplatform.appconfig.entity.OnboardingSlide;
import com.examplatform.appconfig.service.OnboardingService;
import com.examplatform.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/onboarding")
@RequiredArgsConstructor
public class AdminOnboardingController {

    private final OnboardingService service;

    /** GET /admin/onboarding */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OnboardingSlide>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Slides loaded", service.getAll()));
    }

    /** POST /admin/onboarding */
    @PostMapping
    public ResponseEntity<ApiResponse<OnboardingSlide>> create(@RequestBody OnboardingSlideRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Slide created", service.create(req)));
    }

    /** PUT /admin/onboarding/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OnboardingSlide>> update(
            @PathVariable String id,
            @RequestBody OnboardingSlideRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Slide updated", service.update(id, req)));
    }

    /** DELETE /admin/onboarding/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Slide deleted", null));
    }

    /** PUT /admin/onboarding/{id}/toggle */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<OnboardingSlide>> toggle(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Slide status toggled", service.toggleStatus(id)));
    }

    /** PUT /admin/onboarding/reorder — drag-and-drop reorder */
    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<List<OnboardingSlide>>> reorder(@RequestBody SlideReorderRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Slides reordered", service.reorder(req)));
    }
}