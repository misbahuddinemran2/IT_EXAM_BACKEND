package com.examplatform.appconfig.dto;

import lombok.*;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BulkSettingUpdateRequest {
    // key -> value map e.g. {"maintenance_mode": "true", "maintenance_title": "..."}
    private Map<String, String> settings;
}