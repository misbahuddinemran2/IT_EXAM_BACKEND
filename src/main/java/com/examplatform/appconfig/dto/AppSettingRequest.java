package com.examplatform.appconfig.dto;

import lombok.*;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AppSettingRequest {
    private String settingKey;
    private String settingValue;
    private String description;
}