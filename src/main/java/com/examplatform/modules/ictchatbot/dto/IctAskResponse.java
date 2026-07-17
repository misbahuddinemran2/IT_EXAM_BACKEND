package com.examplatform.modules.ictchatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IctAskResponse {
    private String answer;
    private List<String> sourceWriters;   // যে লেখকদের content থেকে উত্তর বানানো হয়েছে
    private List<String> diagramUrls;
    private boolean fromCache;
}
