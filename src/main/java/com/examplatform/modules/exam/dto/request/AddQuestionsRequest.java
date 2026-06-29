package com.examplatform.modules.exam.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddQuestionsRequest {

    // Manually question add করার জন্য
    private List<String> questionIds;
    private double marksPerQuestion;
}