package com.examplatform.modules.exam.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AIExplanationResponse {
    private String questionId;
    private String briefExplanation;
    private String detailedExplanation;
    private List<String> keyPoints;
    private String mnemonicTrick;
    private List<String> relatedTopics;
    private String resourceLink;
}