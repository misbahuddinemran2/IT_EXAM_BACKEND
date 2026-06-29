package com.examplatform.appconfig.service;

import com.examplatform.appconfig.dto.AppConfigResponse;
import com.examplatform.appconfig.dto.AppConfigResponse.*;
import com.examplatform.appconfig.entity.AppSetting;
import com.examplatform.appconfig.entity.AppVersion;
import com.examplatform.appconfig.entity.OnboardingSlide;
import com.examplatform.appconfig.repository.AppSettingRepository;
import com.examplatform.appconfig.repository.AppVersionRepository;
import com.examplatform.appconfig.repository.OnboardingSlideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppConfigService {

    private final AppSettingRepository settingRepo;
    private final AppVersionRepository versionRepo;
    private final OnboardingSlideRepository slideRepo;

    // ─── Public: build full config ─────────────────────────────────────────────

    public AppConfigResponse buildFullConfig() {
        // Load all settings into a key→value map for fast lookup
        Map<String, String> cfg = settingRepo.findAllActiveGrouped()
                .stream()
                .collect(Collectors.toMap(AppSetting::getSettingKey, s -> {
                    String v = s.getSettingValue();
                    return v != null ? v : "";
                }));

        AppVersion version = versionRepo.findFirstByIsActiveTrueOrderByCreatedAtDesc()
                .orElseGet(this::defaultVersion);

        List<OnboardingSlide> slides = slideRepo.findByIsActiveTrueOrderBySlideOrder();

        return AppConfigResponse.builder()
                .maintenance(buildMaintenance(cfg))
                .versionInfo(buildVersionInfo(version))
                .splash(buildSplash(cfg))
                .noticePopup(buildNoticePopup(cfg))
                .homePopup(buildHomePopup(cfg))
                .homeBanner(buildHomeBanner(cfg))
                .examNotice(buildExamNotice(cfg))
                .features(buildFeatures(cfg))
                .support(buildSupport(cfg))
                .socialLinks(buildSocialLinks(cfg))
                .androidApkDownloadUrl(str(cfg, "android_apk_download_url"))
                .systemStatus(str(cfg, "system_status", "OPERATIONAL"))
                .onboardingSlides(mapSlides(slides))
                .build();
    }

    // ─── Section builders ──────────────────────────────────────────────────────

    private MaintenanceInfo buildMaintenance(Map<String, String> cfg) {
        return MaintenanceInfo.builder()
                .isUnderMaintenance(bool(cfg, "maintenance_mode"))
                .title(str(cfg, "maintenance_title", "Under Maintenance"))
                .message(str(cfg, "maintenance_message"))
                .estimatedTime(str(cfg, "maintenance_estimated_time"))
                .build();
    }

    private VersionInfo buildVersionInfo(AppVersion v) {
        return VersionInfo.builder()
                .appVersion(v.getAppVersion())
                .latestVersion(v.getLatestVersion())
                .minimumVersion(v.getMinimumVersion())
                .forceUpdate(v.getForceUpdate())
                .optionalUpdate(v.getOptionalUpdate())
                .updateTitle(v.getUpdateTitle())
                .updateMessage(v.getUpdateMessage())
                .androidApkUrl(v.getAndroidApkUrl())
                .iosAppStoreUrl(v.getIosAppStoreUrl())
                .playStoreUrl(v.getPlayStoreUrl())
                .build();
    }

    private SplashInfo buildSplash(Map<String, String> cfg) {
        return SplashInfo.builder()
                .title(str(cfg, "splash_title", "IT Exam Guru"))
                .subtitle(str(cfg, "splash_subtitle", "Prepare. Practice. Succeed."))
                .imageUrl(str(cfg, "splash_image_url"))
                .durationMs(integer(cfg, "splash_duration_ms", 2000))
                .build();
    }

    private NoticePopupInfo buildNoticePopup(Map<String, String> cfg) {
        return NoticePopupInfo.builder()
                .enabled(bool(cfg, "notice_popup_enabled"))
                .title(str(cfg, "notice_popup_title"))
                .message(str(cfg, "notice_popup_message"))
                .imageUrl(str(cfg, "notice_popup_image_url"))
                .buttonText(str(cfg, "notice_popup_button_text", "Got it"))
                .linkUrl(str(cfg, "notice_popup_link_url"))
                .build();
    }

    private HomePopupInfo buildHomePopup(Map<String, String> cfg) {
        return HomePopupInfo.builder()
                .enabled(bool(cfg, "home_popup_enabled"))
                .title(str(cfg, "home_popup_title"))
                .message(str(cfg, "home_popup_message"))
                .imageUrl(str(cfg, "home_popup_image_url"))
                .build();
    }

    private HomeBannerInfo buildHomeBanner(Map<String, String> cfg) {
        return HomeBannerInfo.builder()
                .enabled(bool(cfg, "home_banner_enabled", true))
                .imageUrl(str(cfg, "home_banner_image_url"))
                .linkUrl(str(cfg, "home_banner_link_url"))
                .build();
    }

    private ExamNoticeInfo buildExamNotice(Map<String, String> cfg) {
        return ExamNoticeInfo.builder()
                .enabled(bool(cfg, "exam_notice_enabled"))
                .message(str(cfg, "exam_notice_message"))
                .build();
    }

    private FeaturesInfo buildFeatures(Map<String, String> cfg) {
        return FeaturesInfo.builder()
                .liveExamEnabled(bool(cfg, "live_exam_enabled"))
                .registrationEnabled(bool(cfg, "registration_enabled", true))
                .guestModeEnabled(bool(cfg, "guest_mode_enabled"))
                .leaderboardEnabled(bool(cfg, "leaderboard_enabled", true))
                .premiumSubscriptionEnabled(bool(cfg, "premium_subscription_enabled", true))
                .adsEnabled(bool(cfg, "ads_enabled"))
                .build();
    }

    private SupportInfo buildSupport(Map<String, String> cfg) {
        return SupportInfo.builder()
                .email(str(cfg, "support_email"))
                .phone(str(cfg, "support_phone"))
                .whatsapp(str(cfg, "support_whatsapp"))
                .build();
    }

    private SocialLinksInfo buildSocialLinks(Map<String, String> cfg) {
        return SocialLinksInfo.builder()
                .facebookPageUrl(str(cfg, "facebook_page_url"))
                .facebookGroupUrl(str(cfg, "facebook_group_url"))
                .youtubeChannelUrl(str(cfg, "youtube_channel_url"))
                .telegramGroupUrl(str(cfg, "telegram_group_url"))
                .websiteUrl(str(cfg, "website_url", "https://itexamguru.com"))
                .build();
    }

    private List<OnboardingSlideDto> mapSlides(List<OnboardingSlide> slides) {
        return slides.stream().map(s -> OnboardingSlideDto.builder()
                .id(s.getId())
                .title(s.getTitle())
                .subtitle(s.getSubtitle())
                .description(s.getDescription())
                .imageUrl(s.getImageUrl())
                .animationUrl(s.getAnimationUrl())
                .slideOrder(s.getSlideOrder())
                .build()).collect(Collectors.toList());
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private String str(Map<String, String> cfg, String key) {
        return cfg.getOrDefault(key, "");
    }

    private String str(Map<String, String> cfg, String key, String defaultVal) {
        String v = cfg.get(key);
        return (v != null && !v.isBlank()) ? v : defaultVal;
    }

    private boolean bool(Map<String, String> cfg, String key) {
        return "true".equalsIgnoreCase(cfg.getOrDefault(key, "false"));
    }

    private boolean bool(Map<String, String> cfg, String key, boolean defaultVal) {
        String v = cfg.get(key);
        if (v == null || v.isBlank()) return defaultVal;
        return "true".equalsIgnoreCase(v);
    }

    private int integer(Map<String, String> cfg, String key, int defaultVal) {
        try {
            return Integer.parseInt(cfg.getOrDefault(key, String.valueOf(defaultVal)));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private AppVersion defaultVersion() {
        return AppVersion.builder()
                .appVersion("1.0.0")
                .latestVersion("1.0.0")
                .minimumVersion("1.0.0")
                .forceUpdate(false)
                .optionalUpdate(false)
                .updateTitle("New Update Available")
                .updateMessage("Please update to the latest version.")
                .androidApkUrl("")
                .iosAppStoreUrl("")
                .playStoreUrl("")
                .build();
    }
}