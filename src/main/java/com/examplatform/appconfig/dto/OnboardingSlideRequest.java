package com.examplatform.appconfig.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OnboardingSlideRequest {
    private String title;
    private String subtitle;
    private String description;
    private String imageUrl;
    private String animationUrl;
    private Integer slideOrder;
    private Boolean isActive;
}