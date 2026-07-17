package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.dto.IctUploadListResponse;
import com.examplatform.modules.ictchatbot.dto.IctUploadResponse;
import com.examplatform.modules.ictchatbot.dto.IctUploadReviewRequest;
import com.examplatform.modules.ictchatbot.entity.IctOcrUpload;
import com.examplatform.modules.ictchatbot.enums.IctUploadStatus;
import com.examplatform.modules.ictchatbot.service.IctUploadService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/uploads")
    public ResponseEntity<List<IctUploadListResponse>> getUploads(
            @RequestParam(value = "status", required = false) String status
    ) {
        IctUploadStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = IctUploadStatus.valueOf(status.toUpperCase());
        }

        List<IctOcrUpload> uploads = uploadService.getUploadsByStatus(statusEnum);

        List<IctUploadListResponse> response = uploads.stream()
                .map(u -> IctUploadListResponse.builder()
                        .id(u.getId())
                        .ocrText(u.getOcrText())
                        .writerName(u.getWriterName())
                        .subjectId(u.getSubjectId())
                        .chapterId(u.getChapterId())
                        .topicId(u.getTopicId())
                        .status(u.getStatus().name())
                        .createdAt(u.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/uploads/{id}")
    public ResponseEntity<IctUploadListResponse> reviewUpload(
            @PathVariable("id") String id,
            @RequestBody IctUploadReviewRequest request
    ) {
        IctOcrUpload updated = uploadService.reviewUpload(id, request);

        IctUploadListResponse response = IctUploadListResponse.builder()
                .id(updated.getId())
                .ocrText(updated.getOcrText())
                .writerName(updated.getWriterName())
                .subjectId(updated.getSubjectId())
                .chapterId(updated.getChapterId())
                .topicId(updated.getTopicId())
                .status(updated.getStatus().name())
                .createdAt(updated.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }
}
