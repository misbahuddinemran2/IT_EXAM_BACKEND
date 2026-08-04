package com.examplatform.modules.evaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Admin panel dataset list view - প্রশ্ন/run সংখ্যা সহ
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetSummaryResponse {
    private String id;
    private String name;
    private String domain;
    private String language;
    private long questionCount;
    private long runCount;
}
