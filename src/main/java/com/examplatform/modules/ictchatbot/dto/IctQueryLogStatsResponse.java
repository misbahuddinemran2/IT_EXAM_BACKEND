package com.examplatform.modules.ictchatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IctQueryLogStatsResponse {

    private long rawLogCount;
}
