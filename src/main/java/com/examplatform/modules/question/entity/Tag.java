package com.examplatform.modules.question.entity;

import com.examplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_type", nullable = false, length = 20)
    private TagType tagType = TagType.CUSTOM;

    @Column(name = "color_code", length = 7)
    @Builder.Default
    private String colorCode = "#6366f1";

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private int usageCount = 0;

    public enum TagType {
        SUBJECT, EXAM_TYPE, DIFFICULTY, TOPIC, CUSTOM
    }
}