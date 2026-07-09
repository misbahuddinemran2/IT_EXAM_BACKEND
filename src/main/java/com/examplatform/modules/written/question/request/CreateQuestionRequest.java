package com.examplatform.modules.written.question.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateQuestionRequest {

    private String examId;

    private String subjectId;   // required
    private String chapterId;   // required
    private String topicId;     // required

    private Integer questionOrder;
    private String stimulus;
    private String stimulusBn;

    private String partAQuestion;
    private String partAModelAnswer;
    private String partAMarkingScheme;
    private BigDecimal partAMaxMark;

    private String partBQuestion;
    private String partBModelAnswer;
    private String partBMarkingScheme;
    private BigDecimal partBMaxMark;

    private String partCQuestion;
    private String partCModelAnswer;
    private String partCMarkingScheme;
    private BigDecimal partCMaxMark;

    private String partDQuestion;
    private String partDModelAnswer;
    private String partDMarkingScheme;
    private BigDecimal partDMaxMark;
}