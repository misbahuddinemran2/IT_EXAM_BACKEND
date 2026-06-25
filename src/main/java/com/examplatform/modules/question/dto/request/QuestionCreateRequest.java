package com.examplatform.modules.question.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionCreateRequest {

    @NotBlank(message = "Question text is required")
    private String questionText;

    private String questionTextBn;

    private String questionType = "MCQ_SINGLE";

    private String language = "EN";

    @NotBlank(message = "Subject ID is required")
    private String subjectId;

    @NotBlank(message = "Chapter ID is required")
    private String chapterId;

    @NotBlank(message = "Topic ID is required")
    private String topicId;

    @Min(1) @Max(5)
    private int difficultyLevel = 3;

    private String cognitiveLevel = "REMEMBER";

    private int estimatedTimeSec = 60;

    private String sourceReference;

    private Integer yearAppeared;

    @Valid
    @NotEmpty(message = "At least one option is required")
    private List<OptionRequest> options;

    // Mappings
    private List<String> conceptIds;
    private List<String> tagIds;
    private List<String> examTypeIds;
}