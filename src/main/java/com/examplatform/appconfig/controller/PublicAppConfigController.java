package com.examplatform.appconfig.controller;

import com.examplatform.appconfig.dto.AppConfigResponse;
import com.examplatform.appconfig.service.AppConfigService;
import com.examplatform.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/app-config")
@RequiredArgsConstructor
public class PublicAppConfigController {

    private final AppConfigService appConfigService;

    /**
     * GET /api/v1/public/app-config
     *
     * React Native startup call — returns everything the app needs at launch:
     *   maintenance → versionInfo → noticePopup → onboarding → features → social
     *
     * Lightweight, no auth required, cache-friendly.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AppConfigResponse>> getAppConfig() {
        AppConfigResponse config = appConfigService.buildFullConfig();
        return ResponseEntity.ok(ApiResponse.success("App config loaded successfully", config));
    }
}