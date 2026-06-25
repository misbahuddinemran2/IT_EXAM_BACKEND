package com.examplatform.modules.examtype.entity;

import com.examplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamType extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "name_bn", length = 100)
    private String nameBn;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "conducting_body", length = 200)
    private String conductingBody;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}