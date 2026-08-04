package com.examplatform.modules.evaluation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunTriggerRequest {
    private String datasetId;
    private String profileId;
    private String promptId;
    private String datasetVersion;   // optional, reproducibility tag
}
