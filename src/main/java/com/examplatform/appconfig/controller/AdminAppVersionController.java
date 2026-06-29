package com.examplatform.appconfig.controller;

import com.examplatform.appconfig.dto.AppVersionRequest;
import com.examplatform.appconfig.entity.AppVersion;
import com.examplatform.appconfig.service.AppVersionService;
import com.examplatform.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/app-version")
@RequiredArgsConstructor
public class AdminAppVersionController {

    private final AppVersionService service;

    /** GET /admin/app-version/current */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<AppVersion>> getCurrent() {
        return ResponseEntity.ok(ApiResponse.success("Current version loaded", service.getCurrent()));
    }

    /** GET /admin/app-version/history */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<AppVersion>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Version history loaded", service.getAll()));
    }

    /**
     * POST /admin/app-version
     * Creates a new active version config (marks previous ones inactive).
     * Use this to publish a new version.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AppVersion>> create(@RequestBody AppVersionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Version config saved", service.save(req)));
    }

    /** PUT /admin/app-version/{id} — update specific version record */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppVersion>> update(
            @PathVariable String id,
            @RequestBody AppVersionRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Version updated", service.update(id, req)));
    }
}