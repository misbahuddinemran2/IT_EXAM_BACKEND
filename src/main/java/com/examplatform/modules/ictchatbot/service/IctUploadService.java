package com.examplatform.modules.ictchatbot.service;

import com.examplatform.modules.ictchatbot.dto.IctUploadReviewRequest;
import com.examplatform.modules.ictchatbot.entity.IctOcrUpload;
import com.examplatform.modules.ictchatbot.enums.IctUploadStatus;
import com.examplatform.modules.ictchatbot.repository.IctOcrUploadRepository;
import com.examplatform.modules.written.submission.service.ImageKitUploadService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IctUploadService {

    private final IctOcrUploadRepository uploadRepository;
    private final OcrService ocrService;
    private final ImageKitUploadService imageKitUploadService;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final String IMAGEKIT_FOLDER = "/ict-chatbot";

    public IctOcrUpload uploadAndOcr(MultipartFile file, String writerName,
                                      String subjectId, String chapterId, String topicId) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 2MB limit");
        }

        // 1. ImageKit এ আপলোড করো
        String imageUrl = imageKitUploadService.uploadFile(file, IMAGEKIT_FOLDER);

        // 2. OCR.space দিয়ে টেক্সট বের করো
        String ocrText = ocrService.extractTextFromImageUrl(imageUrl);

        if (ocrText == null || ocrText.isBlank()) {
            throw new RuntimeException("OCR failed to extract any text from the image");
        }

        // 3. DB তে সেভ করো
        IctOcrUpload upload = IctOcrUpload.builder()
                .ocrText(ocrText)
                .writerName(writerName)
                .subjectId(subjectId)
                .chapterId(chapterId)
                .topicId(topicId)
                .status(IctUploadStatus.PENDING)
                .build();

        return uploadRepository.save(upload);
    }

    public List<IctOcrUpload> getUploadsByStatus(IctUploadStatus status) {
        if (status == null) {
            return uploadRepository.findAll();
        }
        return uploadRepository.findByStatusOrderByCreatedAtAsc(status);
    }

    public IctOcrUpload reviewUpload(String id, IctUploadReviewRequest request) {
        IctOcrUpload upload = uploadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found: " + id));

        if (request.getOcrText() != null && !request.getOcrText().isBlank()) {
            upload.setOcrText(request.getOcrText());
        }
        if (request.getWriterName() != null) {
            upload.setWriterName(request.getWriterName());
        }
        if (request.getSubjectId() != null) {
            upload.setSubjectId(request.getSubjectId());
        }
        if (request.getChapterId() != null) {
            upload.setChapterId(request.getChapterId());
        }
        if (request.getTopicId() != null) {
            upload.setTopicId(request.getTopicId());
        }

        upload.setStatus(IctUploadStatus.REVIEWED);
        upload.setReviewedByAdminId(request.getReviewedByAdminId());
        upload.setReviewedAt(LocalDateTime.now());

        return uploadRepository.save(upload);
    }
}
