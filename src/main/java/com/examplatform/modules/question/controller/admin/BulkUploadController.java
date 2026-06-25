package com.examplatform.modules.question.controller.admin;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.modules.question.dto.response.BulkUploadResultResponse;
import com.examplatform.modules.question.service.BulkUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class BulkUploadController {

    private final BulkUploadService bulkUploadService;

    @PostMapping(
            value = "/admin/bulk-upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<BulkUploadResultResponse>>
    upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam String subjectId,
            @RequestParam String chapterId,
            @RequestParam String topicId
    )
            throws Exception {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File is empty"));
        }

        String filename = file.getOriginalFilename();
        if (filename == null ||
                !filename.endsWith(".xlsx")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(
                        "Only .xlsx files allowed"));
        }

        BulkUploadResultResponse result =
                bulkUploadService.uploadAndImport(
                        file,
                        subjectId,
                        chapterId,
                        topicId
                );

        return ResponseEntity.ok(
            ApiResponse.success("Upload completed", result)
        );
    }
}