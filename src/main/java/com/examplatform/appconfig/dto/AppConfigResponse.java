package com.examplatform.appconfig.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppConfigResponse {

    private MaintenanceInfo maintenance;
    private VersionInfo versionInfo;
    private SplashInfo splash;
    private NoticePopupInfo noticePopup;
    private HomePopupInfo homePopup;
    private HomeBannerInfo homeBanner;
    private ExamNoticeInfo examNotice;
    private FeaturesInfo features;
    private SupportInfo support;
    private SocialLinksInfo socialLinks;
    private String androidApkDownloadUrl;
    private String systemStatus;
    private List<OnboardingSlideDto> onboardingSlides;

    // ─── Nested DTOs ──────────────────────────────────────────────────────────

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MaintenanceInfo {
        private Boolean isUnderMaintenance;
        private String title;
        private String message;
        private String estimatedTime;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class VersionInfo {
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

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SplashInfo {
        private String title;
        private String subtitle;
        private String imageUrl;
        private Integer durationMs;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NoticePopupInfo {
        private Boolean enabled;
        private String title;
        private String message;
        private String imageUrl;
        private String buttonText;
        private String linkUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class HomePopupInfo {
        private Boolean enabled;
        private String title;
        private String message;
        private String imageUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class HomeBannerInfo {
        private Boolean enabled;
        private String imageUrl;
        private String linkUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ExamNoticeInfo {
        private Boolean enabled;
        private String message;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FeaturesInfo {
        private Boolean liveExamEnabled;
        private Boolean registrationEnabled;
        private Boolean guestModeEnabled;
        private Boolean leaderboardEnabled;
        private Boolean premiumSubscriptionEnabled;
        private Boolean adsEnabled;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SupportInfo {
        private String email;
        private String phone;
        private String whatsapp;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SocialLinksInfo {
        private String facebookPageUrl;
        private String facebookGroupUrl;
        private String youtubeChannelUrl;
        private String telegramGroupUrl;
        private String websiteUrl;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OnboardingSlideDto {
        private String id;
        private String title;
        private String subtitle;
        private String description;
        private String imageUrl;
        private String animationUrl;
        private Integer slideOrder;
    }
}