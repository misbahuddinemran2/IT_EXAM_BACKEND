package com.examplatform.appconfig.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "app_version")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppVersion {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "app_version", nullable = false, length = 20)
    private String appVersion;

    @Column(name = "latest_version", nullable = false, length = 20)
    private String latestVersion;

    @Column(name = "minimum_version", nullable = false, length = 20)
    private String minimumVersion;

    @Column(name = "force_update")
    @Builder.Default
    private Boolean forceUpdate = false;

    @Column(name = "optional_update")
    @Builder.Default
    private Boolean optionalUpdate = false;

    @Column(name = "update_title", length = 255)
    @Builder.Default
    private String updateTitle = "New Update Available";

    @Column(name = "update_message", columnDefinition = "TEXT")
    private String updateMessage;

    @Column(name = "android_apk_url", length = 500)
    private String androidApkUrl;

    @Column(name = "ios_app_store_url", length = 500)
    private String iosAppStoreUrl;

    @Column(name = "play_store_url", length = 500)
    private String playStoreUrl;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}