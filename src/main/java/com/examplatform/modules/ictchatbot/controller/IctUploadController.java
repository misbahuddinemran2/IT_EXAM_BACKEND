package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.dto.IctUploadResponse;
import com.examplatform.modules.ictchatbot.entity.IctOcrUpload;
import com.examplatform.modules.ictchatbot.service.IctUploadService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/ict")
@RequiredArgsConstructor
public class IctUploadController {

    private final IctUploadService uploadService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<IctUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("writerName") String writerName,
            @RequestParam(value = "subjectId", required = false) String subjectId,
            @RequestParam(value = "chapterId", required = false) String chapterId,
            @RequestParam(value = "topicId", required = false) String topicId
    ) {
        IctOcrUpload saved = uploadService.uploadAndOcr(file, writerName, subjectId, chapterId, topicId);

        IctUploadResponse response = IctUploadResponse.builder()
                .id(saved.getId())
                .ocrText(saved.getOcrText())
                .writerName(saved.getWriterName())
                .status(saved.getStatus().name())
                .build();

        return ResponseEntity.ok(response);
    }
}
