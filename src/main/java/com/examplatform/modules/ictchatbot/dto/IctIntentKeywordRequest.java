package com.examplatform.modules.ictchatbot.dto;

import lombok.Data;

@Data
public class IctIntentKeywordRequest {
    private String intent;   // enum name হিসেবে string, যেমন "FEATURES"
    private String keyword;
    private Boolean isActive;
}
