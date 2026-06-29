package com.examplatform.appconfig.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AppVersionRequest {
    private String appVersion;
    private String latestVersion;
    private String minimumVersion;
    private Boolean forceUpdate;
    private Boolean optionalUpdate;
    private String updateTitle;
    private String updateMessage;
    private String androidApkUrl;
    private String iosAppStoreUrl;
    private String playStoreUrl;
}