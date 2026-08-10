package com.examplatform.modules.practical.controller;

import com.examplatform.modules.practical.service.PracticalStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/practical")
@RequiredArgsConstructor
public class PracticalStudentController {

    private final PracticalStudentService practicalStudentService;

    @GetMapping("/chapters")
    public ResponseEntity<?> getChapters(@RequestHeader("X-User-Id") String userId) {
        try {
            var chapters = practicalStudentService.getChapters(userId);
            return ResponseEntity.ok(Map.of("success", true, "data", chapters));
        } catch (Exception ex) {
            log.error("Error fetching practical chapters", ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @GetMapping("/chapters/{chapterId}/experiments")
    public ResponseEntity<?> getExperiments(@PathVariable String chapterId,
                                             @RequestHeader("X-User-Id") String userId) {
        try {
            var experiments = practicalStudentService.getExperiments(chapterId, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", experiments));
        } catch (Exception ex) {
            log.error("Error fetching experiments for chapter {}", chapterId, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @GetMapping("/experiments/{experimentId}")
    public ResponseEntity<?> getExperimentDetail(@PathVariable String experimentId,
                                                  @RequestHeader("X-User-Id") String userId) {
        try {
            var detail = practicalStudentService.getExperimentDetail(experimentId, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", detail));
        } catch (Exception ex) {
            log.error("Error fetching experiment detail {}", experimentId, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
