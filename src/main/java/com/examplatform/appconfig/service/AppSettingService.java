package com.examplatform.appconfig.service;

import com.examplatform.appconfig.dto.AppSettingRequest;
import com.examplatform.appconfig.dto.BulkSettingUpdateRequest;
import com.examplatform.appconfig.entity.AppSetting;
import com.examplatform.appconfig.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppSettingService {

    private final AppSettingRepository repo;

    public List<AppSetting> getAll() {
        return repo.findAllActiveGrouped();
    }

    public Map<String, List<AppSetting>> getAllGroupedByCategory() {
        return repo.findAllActiveGrouped()
                .stream()
                .collect(Collectors.groupingBy(AppSetting::getCategory));
    }

    public AppSetting getByKey(String key) {
        return repo.findBySettingKey(key)
                .orElseThrow(() -> new RuntimeException("Setting not found: " + key));
    }

    @Transactional
    public AppSetting updateSetting(String key, AppSettingRequest req) {
        AppSetting setting = repo.findBySettingKey(key)
                .orElseThrow(() -> new RuntimeException("Setting not found: " + key));
        setting.setSettingValue(req.getSettingValue());
        if (req.getDescription() != null) setting.setDescription(req.getDescription());
        return repo.save(setting);
    }

    @Transactional
    public Map<String, String> bulkUpdate(BulkSettingUpdateRequest req) {
        Map<String, String> result = new LinkedHashMap<>();
        req.getSettings().forEach((key, value) -> {
            repo.findBySettingKey(key).ifPresent(setting -> {
                setting.setSettingValue(value);
                repo.save(setting);
                result.put(key, value);
            });
        });
        return result;
    }

    @Transactional
    public AppSetting createSetting(AppSettingRequest req) {
        if (repo.findBySettingKey(req.getSettingKey()).isPresent()) {
            throw new RuntimeException("Setting key already exists: " + req.getSettingKey());
        }
        AppSetting s = AppSetting.builder()
                .settingKey(req.getSettingKey())
                .settingValue(req.getSettingValue())
                .description(req.getDescription())
                .build();
        return repo.save(s);
    }
}