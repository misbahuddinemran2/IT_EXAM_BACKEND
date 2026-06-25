package com.examplatform.modules.question.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BulkUploadResultResponse {
    private String jobId;
    private String fileName;
    private int totalRows;
    private int validRows;
    private int failedRows;
    private int importedRows;
    private String status;
    private List<RowError> errors;

    @Getter
    @Builder
    public static class RowError {
        private int rowNumber;
        private String errorMessage;
    }
}