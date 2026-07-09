package com.examplatform.modules.written.submission.service.impl;

import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.enums.ExamStatus;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.entity.WrittenSubmissionFile;
import com.examplatform.modules.written.submission.enums.SubmissionStatus;
import com.examplatform.modules.written.submission.mapper.WrittenSubmissionMapper;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionFileRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import com.examplatform.modules.written.submission.request.StartExamRequest;
import com.examplatform.modules.written.submission.request.SubmitExamRequest;
import com.examplatform.modules.written.submission.request.UploadSubmissionFileRequest;
import com.examplatform.modules.written.submission.response.SubmissionFileResponse;
import com.examplatform.modules.written.submission.response.SubmissionResponse;
import com.examplatform.modules.written.submission.service.WrittenSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WrittenSubmissionServiceImpl implements WrittenSubmissionService {

    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenSubmissionFileRepository fileRepository;
    private final WrittenExamRepository examRepository;
    private final WrittenSubmissionMapper submissionMapper;

    @Override
    @Transactional
    public SubmissionResponse startExam(String userId, StartExamRequest request) {
        WrittenExam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + request.getExamId()));

        if (request.isPracticeMode()) {
            return startPracticeAttempt(exam, userId);
        }
        return startLiveAttempt(exam, userId);
    }

    private SubmissionResponse startLiveAttempt(WrittenExam exam, String userId) {
        if (exam.getStatus() != ExamStatus.LIVE) {
            throw new IllegalStateException("Exam is not currently LIVE");
        }

        boolean alreadyAttempted = submissionRepository
                .existsByExamIdAndUserIdAndCycleNumberAndIsPracticeModeFalse(
                        exam.getId(), userId, exam.getCycleNumber());

        if (alreadyAttempted) {
            throw new IllegalStateException("You have already attempted this exam in the current cycle");
        }

        WrittenSubmission submission = WrittenSubmission.builder()
                .examId(exam.getId())
                .userId(userId)
                .cycleNumber(exam.getCycleNumber())
                .attemptNumber(1)
                .status(SubmissionStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .isPracticeMode(false)
                .build();

        return submissionMapper.toResponse(submissionRepository.save(submission));
    }

    private SubmissionResponse startPracticeAttempt(WrittenExam exam, String userId) {
        // Practice শুধু ENDED/ARCHIVED exam-এর ওপর করা উচিত (Practice Archive থেকে)
        if (exam.getStatus() != ExamStatus.ENDED && exam.getStatus() != ExamStatus.ARCHIVED) {
            throw new IllegalStateException("Practice is only allowed on ended/archived exams");
        }

        long previousPracticeCount = submissionRepository
                .countByExamIdAndUserIdAndIsPracticeModeTrue(exam.getId(), userId);

        WrittenSubmission submission = WrittenSubmission.builder()
                .examId(exam.getId())
                .userId(userId)
                .cycleNumber(exam.getCycleNumber())
                .attemptNumber((int) previousPracticeCount + 1)
                .status(SubmissionStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .isPracticeMode(true)
                .build();

        return submissionMapper.toResponse(submissionRepository.save(submission));
    }

    @Override
    @Transactional
    public SubmissionFileResponse uploadFile(String submissionId, String userId, UploadSubmissionFileRequest request) {
        WrittenSubmission submission = getOwnedSubmissionOrThrow(submissionId, userId);

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot upload file, submission is not in progress");
        }

        WrittenSubmissionFile file = WrittenSubmissionFile.builder()
                .submissionId(submissionId)
                .pageNumber(request.getPageNumber())
                .fileUrl(request.getFileUrl())
                .fileType(request.getFileType())
                .build();

        return submissionMapper.toFileResponse(fileRepository.save(file));
    }

    @Override
    @Transactional
    public SubmissionResponse submitExam(String submissionId, String userId, SubmitExamRequest request) {
        WrittenSubmission submission = getOwnedSubmissionOrThrow(submissionId, userId);

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Submission is not in progress");
        }

        long fileCount = fileRepository.countBySubmissionId(submissionId);
        if (fileCount == 0) {
            throw new IllegalStateException("Cannot submit without uploading at least one page");
        }

        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());

        return submissionMapper.toResponse(submissionRepository.save(submission));
    }

    @Override
    public SubmissionResponse getSubmissionById(String submissionId, String userId) {
        return submissionMapper.toResponse(getOwnedSubmissionOrThrow(submissionId, userId));
    }

    @Override
    public List<SubmissionFileResponse> getSubmissionFiles(String submissionId, String userId) {
        getOwnedSubmissionOrThrow(submissionId, userId); // ownership check
        return fileRepository.findBySubmissionIdOrderByPageNumberAsc(submissionId).stream()
                .map(submissionMapper::toFileResponse)
                .toList();
    }

    @Override
    public List<SubmissionResponse> getMySubmissions(String userId) {
        return submissionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(submissionMapper::toResponse)
                .toList();
    }

    @Override
    public List<SubmissionResponse> getSubmissionsForExam(String examId) {
        return submissionRepository.findByExamIdAndStatus(examId, SubmissionStatus.SUBMITTED).stream()
                .map(submissionMapper::toResponse)
                .toList();
    }

    private WrittenSubmission getOwnedSubmissionOrThrow(String submissionId, String userId) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        if (!submission.getUserId().equals(userId)) {
            throw new IllegalStateException("You do not have access to this submission");
        }
        return submission;
    }
}