package com.examplatform.modules.guide.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GuidePracticeMcqAdminRequest {
    private String topicId;
    private String questionText;
    private String questionTextBn;
    private boolean isBoardQuestion;
    private String board;
    private Integer yearAppeared;
    private int sortOrder;
    private List<OptionItem> options;

    @Getter
    @Setter
    public static class OptionItem {
        private String optionKey;
        private String optionText;
        private String optionTextBn;
        private boolean isCorrect;
        private String explanation;
        private int orderIndex;
    }
}
