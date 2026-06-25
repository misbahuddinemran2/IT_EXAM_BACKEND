package com.examplatform.modules.taxonomy.entity;

import com.examplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chapters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "name_bn", length = 200)
    private String nameBn;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "chapter", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Topic> topics = new ArrayList<>();
}