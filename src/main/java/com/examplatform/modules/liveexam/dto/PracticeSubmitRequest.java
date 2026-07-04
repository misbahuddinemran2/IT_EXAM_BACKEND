package com.examplatform.modules.liveexam.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PracticeSubmitRequest {
    private Map<String, String> answers; // questionId -> selectedOptionId
}
