package com.examplatform.modules.practical.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "practical_chapters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticalChapter {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "name_bn", length = 100)
    private String nameBn;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "order_number", nullable = false)
    @Builder.Default
    private int orderNumber = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
