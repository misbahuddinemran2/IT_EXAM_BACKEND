package com.examplatform.modules.ictchatbot.dto;

import lombok.Data;

@Data
public class IctQuickReplyRequest {
    private String keywords;      // comma-separated: "কেমন আছ,কি অবস্থা"
    private String replyText;
    private Boolean isActive;     // update এ optional, null হলে unchanged থাকবে
}
