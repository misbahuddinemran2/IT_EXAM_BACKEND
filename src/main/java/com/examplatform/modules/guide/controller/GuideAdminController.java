package com.examplatform.modules.guide.controller;

import com.examplatform.modules.guide.dto.GuideContentAdminRequest;
import com.examplatform.modules.guide.service.GuideContentAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("admin/guide")
@RequiredArgsConstructor
public class GuideAdminController {

    private final GuideContentAdminService guideContentAdminService;

    @GetMapping("/contents")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(Map.of("success", true, "data", guideContentAdminService.getAll()));
    }

    @GetMapping("/contents/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("success", true, "data", guideContentAdminService.getById(id)));
    }

    @PostMapping("/contents")
    public ResponseEntity<?> create(@RequestBody GuideContentAdminRequest req) {
        try {
            return ResponseEntity.ok(Map.of("success", true, "data", guideContentAdminService.create(req)));
        } catch (Exception ex) {
            log.error("Error creating guide content", ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PutMapping("/contents/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody GuideContentAdminRequest req) {
        try {
            return ResponseEntity.ok(Map.of("success", true, "data", guideContentAdminService.update(id, req)));
        } catch (Exception ex) {
            log.error("Error updating guide content {}", id, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PutMapping("/contents/{id}/publish")
    public ResponseEntity<?> publish(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("success", true, "data", guideContentAdminService.publish(id)));
    }

    @PutMapping("/contents/{id}/archive")
    public ResponseEntity<?> archive(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("success", true, "data", guideContentAdminService.archive(id)));
    }

    @DeleteMapping("/contents/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        guideContentAdminService.delete(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Deleted"));
    }
}
