package com.examplatform.modules.evaluation.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

// bulk-upload-এর existing প্যাটার্নের মতো একসাথে অনেক প্রশ্ন insert করার জন্য
@Getter
@Setter
public class QuestionBulkUploadRequest {
    private String datasetId;
    private List<QuestionCreateRequest> questions;
}
