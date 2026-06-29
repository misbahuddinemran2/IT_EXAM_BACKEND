package com.examplatform.appconfig.controller;

import com.examplatform.appconfig.dto.AppSettingRequest;
import com.examplatform.appconfig.dto.BulkSettingUpdateRequest;
import com.examplatform.appconfig.entity.AppSetting;
import com.examplatform.appconfig.service.AppSettingService;
import com.examplatform.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/app-settings")
@RequiredArgsConstructor
public class AdminAppSettingController {

    private final AppSettingService service;

    /** GET /admin/app-settings — all settings flat list */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AppSetting>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Settings loaded", service.getAll()));
    }

    /** GET /admin/app-settings/grouped — grouped by category */
    @GetMapping("/grouped")
    public ResponseEntity<ApiResponse<Map<String, List<AppSetting>>>> getGrouped() {
        return ResponseEntity.ok(ApiResponse.success("Settings loaded", service.getAllGroupedByCategory()));
    }

    /** GET /admin/app-settings/{key} */
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<AppSetting>> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success("Setting loaded", service.getByKey(key)));
    }

    /** PUT /admin/app-settings/{key} — update single setting */
    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<AppSetting>> update(
            @PathVariable String key,
            @RequestBody AppSettingRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Setting updated", service.updateSetting(key, req)));
    }

    /** POST /admin/app-settings/bulk — update multiple at once */
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<Map<String, String>>> bulkUpdate(
            @RequestBody BulkSettingUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Settings updated", service.bulkUpdate(req)));
    }

    /** POST /admin/app-settings — create a new custom setting key */
    @PostMapping
    public ResponseEntity<ApiResponse<AppSetting>> create(@RequestBody AppSettingRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Setting created", service.createSetting(req)));
    }

    // ─── Category-specific convenience endpoints ──────────────────────────────

    /** PUT /admin/app-settings/maintenance/toggle */
    @PutMapping("/maintenance/toggle")
    public ResponseEntity<ApiResponse<AppSetting>> toggleMaintenance(@RequestParam boolean enabled) {
        AppSettingRequest req = new AppSettingRequest("maintenance_mode", String.valueOf(enabled), null);
        return ResponseEntity.ok(ApiResponse.success("Maintenance mode updated",
                service.updateSetting("maintenance_mode", req)));
    }
}