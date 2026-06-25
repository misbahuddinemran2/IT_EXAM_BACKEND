package com.examplatform.modules.question.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OptionResponse {
    private String id;
    private String optionKey;
    private String optionText;
    private String optionTextBn;

    @JsonProperty("isCorrect")
    private boolean isCorrect;

    @JsonIgnore
    private boolean correct;

    private String explanation;
    private int orderIndex;
}