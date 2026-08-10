package com.examplatform.modules.practical.controller;

import com.examplatform.modules.practical.dto.ExperimentAdminRequest;
import com.examplatform.modules.practical.service.PracticalAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("admin/practical")
@RequiredArgsConstructor
public class PracticalAdminController {

    private final PracticalAdminService practicalAdminService;

    @GetMapping("/chapters")
    public ResponseEntity<?> getAllChapters() {
        return ResponseEntity.ok(Map.of("success", true, "data", practicalAdminService.getAllChapters()));
    }

    @GetMapping("/chapters/{chapterId}/experiments")
    public ResponseEntity<?> getExperiments(@PathVariable String chapterId) {
        return ResponseEntity.ok(Map.of("success", true, "data",
                practicalAdminService.getExperimentsByChapter(chapterId)));
    }

    @PostMapping("/experiments")
    public ResponseEntity<?> createExperiment(@RequestBody ExperimentAdminRequest req,
                                               Authentication authentication) {
        try {
            String adminId = authentication != null ? authentication.getName() : "system";
            var experiment = practicalAdminService.createExperiment(req, adminId);
            return ResponseEntity.ok(Map.of("success", true, "data", experiment));
        } catch (Exception ex) {
            log.error("Error creating practical experiment", ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PutMapping("/experiments/{experimentId}")
    public ResponseEntity<?> updateExperiment(@PathVariable String experimentId,
                                               @RequestBody ExperimentAdminRequest req) {
        try {
            var experiment = practicalAdminService.updateExperiment(experimentId, req);
            return ResponseEntity.ok(Map.of("success", true, "data", experiment));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PatchMapping("/experiments/{experimentId}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable String experimentId,
                                           @RequestBody Map<String, Boolean> body) {
        try {
            practicalAdminService.toggleActive(experimentId, Boolean.TRUE.equals(body.get("isActive")));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @DeleteMapping("/experiments/{experimentId}")
    public ResponseEntity<?> deleteExperiment(@PathVariable String experimentId) {
        try {
            practicalAdminService.deleteExperiment(experimentId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
