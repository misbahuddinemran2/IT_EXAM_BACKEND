package com.examplatform.modules.written.submission.mapper;

import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.entity.WrittenSubmissionFile;
import com.examplatform.modules.written.submission.response.SubmissionFileResponse;
import com.examplatform.modules.written.submission.response.SubmissionResponse;
import org.springframework.stereotype.Component;

@Component
public class WrittenSubmissionMapper {

    public SubmissionResponse toResponse(WrittenSubmission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .examId(s.getExamId())
                .userId(s.getUserId())
                .cycleNumber(s.getCycleNumber())
                .attemptNumber(s.getAttemptNumber())
                .status(s.getStatus().name())
                .startedAt(s.getStartedAt())
                .submittedAt(s.getSubmittedAt())
                .totalObtainedMark(s.getTotalObtainedMark())
                .isPracticeMode(s.isPracticeMode())
                .createdAt(s.getCreatedAt())
                .build();
    }

    public SubmissionFileResponse toFileResponse(WrittenSubmissionFile f) {
        return SubmissionFileResponse.builder()
                .id(f.getId())
                .submissionId(f.getSubmissionId())
                .pageNumber(f.getPageNumber())
                .fileUrl(f.getFileUrl())
                .fileType(f.getFileType())
                .uploadedAt(f.getUploadedAt())
                .build();
    }
}