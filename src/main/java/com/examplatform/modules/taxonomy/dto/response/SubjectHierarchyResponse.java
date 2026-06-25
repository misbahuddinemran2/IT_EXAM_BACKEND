package com.examplatform.modules.taxonomy.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SubjectHierarchyResponse {
    private String id;
    private String name;
    private String nameBn;
    private String code;
    private List<ChapterHierarchy> chapters;

    @Getter
    @Builder
    public static class ChapterHierarchy {
        private String id;
        private String name;
        private String nameBn;
        private int orderIndex;
        private List<TopicItem> topics;
    }

    @Getter
    @Builder
    public static class TopicItem {
        private String id;
        private String name;
        private String nameBn;
        private int orderIndex;
    }
}