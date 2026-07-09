package com.examplatform.modules.written.evaluation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDetailResponse {

    private String id;
    private String questionId;
    private Integer questionOrder;
    private String part;
    private BigDecimal obtainedMark;
    private BigDecimal maxMark;
    private String feedback;
}