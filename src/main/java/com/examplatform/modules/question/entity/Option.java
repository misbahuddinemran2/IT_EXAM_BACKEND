package com.examplatform.modules.question.entity;

import com.examplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Option extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "option_key", nullable = false, length = 1)
    private String optionKey;

    @Column(name = "option_text",
            columnDefinition = "TEXT", nullable = false)
    private String optionText;

    @Column(name = "option_text_bn", columnDefinition = "TEXT")
    private String optionTextBn;

    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private boolean isCorrect = false;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "explanation_bn", columnDefinition = "TEXT")
    private String explanationBn;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private int orderIndex = 0;

    @Column(name = "selection_count", nullable = false)
    @Builder.Default
    private long selectionCount = 0;
}