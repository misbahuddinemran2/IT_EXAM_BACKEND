package com.examplatform.appconfig.service;

import com.examplatform.appconfig.dto.AppVersionRequest;
import com.examplatform.appconfig.entity.AppVersion;
import com.examplatform.appconfig.repository.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppVersionService {

    private final AppVersionRepository repo;

    public AppVersion getCurrent() {
        return repo.findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new RuntimeException("No active version config found"));
    }

    public List<AppVersion> getAll() {
        return repo.findAll();
    }

    @Transactional
    public AppVersion save(AppVersionRequest req) {
        // Deactivate existing active versions
        repo.findAll().forEach(v -> {
            v.setIsActive(false);
            repo.save(v);
        });

        AppVersion version = AppVersion.builder()
                .appVersion(req.getAppVersion())
                .latestVersion(req.getLatestVersion())
                .minimumVersion(req.getMinimumVersion())
                .forceUpdate(req.getForceUpdate() != null ? req.getForceUpdate() : false)
                .optionalUpdate(req.getOptionalUpdate() != null ? req.getOptionalUpdate() : false)
                .updateTitle(req.getUpdateTitle())
                .updateMessage(req.getUpdateMessage())
                .androidApkUrl(req.getAndroidApkUrl())
                .iosAppStoreUrl(req.getIosAppStoreUrl())
                .playStoreUrl(req.getPlayStoreUrl())
                .isActive(true)
                .build();
        return repo.save(version);
    }

    @Transactional
    public AppVersion update(String id, AppVersionRequest req) {
        AppVersion v = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Version not found: " + id));
        v.setAppVersion(req.getAppVersion());
        v.setLatestVersion(req.getLatestVersion());
        v.setMinimumVersion(req.getMinimumVersion());
        v.setForceUpdate(req.getForceUpdate() != null ? req.getForceUpdate() : false);
        v.setOptionalUpdate(req.getOptionalUpdate() != null ? req.getOptionalUpdate() : false);
        v.setUpdateTitle(req.getUpdateTitle());
        v.setUpdateMessage(req.getUpdateMessage());
        v.setAndroidApkUrl(req.getAndroidApkUrl());
        v.setIosAppStoreUrl(req.getIosAppStoreUrl());
        v.setPlayStoreUrl(req.getPlayStoreUrl());
        return repo.save(v);
    }
}