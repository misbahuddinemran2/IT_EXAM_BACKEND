package com.examplatform.modules.evaluation.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

// একাধিক run পাশাপাশি তুলনা করার request (Gemini vs Claude vs GPT)
@Getter
@Setter
public class RunComparisonRequest {
    private List<String> runIds;
}
