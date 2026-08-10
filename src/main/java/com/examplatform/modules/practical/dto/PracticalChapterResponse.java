package com.examplatform.modules.practical.dto;

import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PracticalChapterResponse {
    private String id;
    private String name;
    private String nameBn;
    private String icon;
    private int experimentCount; // student session অনুযায়ী visible experiment সংখ্যা
}
