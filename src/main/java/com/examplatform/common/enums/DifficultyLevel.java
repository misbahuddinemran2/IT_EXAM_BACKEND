package com.examplatform.common.enums;

public enum DifficultyLevel {
    EASY(1),
    EASY_MEDIUM(2),
    MEDIUM(3),
    HARD(4),
    EXPERT(5);

    private final int value;

    DifficultyLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}