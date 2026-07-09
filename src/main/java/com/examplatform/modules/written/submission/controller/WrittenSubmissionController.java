package com.examplatform.modules.written.submission.controller;

import com.examplatform.modules.written.submission.request.StartExamRequest;
import com.examplatform.modules.written.submission.request.SubmitExamRequest;
import com.examplatform.modules.written.submission.request.UploadSubmissionFileRequest;
import com.examplatform.modules.written.submission.response.SubmissionFileResponse;
import com.examplatform.modules.written.submission.response.SubmissionResponse;
import com.examplatform.modules.written.submission.service.WrittenSubmissionService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/written/submissions")
@RequiredArgsConstructor
public class WrittenSubmissionController {

    private final WrittenSubmissionService submissionService;

    @PostMapping("/start")
    public SubmissionResponse startExam(@RequestBody StartExamRequest request, Authentication auth) {
        return submissionService.startExam(auth.getName(), request);
    }

    @PostMapping("/{submissionId}/upload")
    public SubmissionFileResponse uploadFile(
            @PathVariable String submissionId,
            @RequestBody UploadSubmissionFileRequest request,
            Authentication auth) {
        return submissionService.uploadFile(submissionId, auth.getName(), request);
    }

    @PostMapping("/{submissionId}/submit")
    public SubmissionResponse submitExam(
            @PathVariable String submissionId,
            @RequestBody SubmitExamRequest request,
            Authentication auth) {
        return submissionService.submitExam(submissionId, auth.getName(), request);
    }

    @GetMapping("/{submissionId}")
    public SubmissionResponse getSubmission(@PathVariable String submissionId, Authentication auth) {
        return submissionService.getSubmissionById(submissionId, auth.getName());
    }

    @GetMapping("/{submissionId}/files")
    public List<SubmissionFileResponse> getFiles(@PathVariable String submissionId, Authentication auth) {
        return submissionService.getSubmissionFiles(submissionId, auth.getName());
    }

    @GetMapping("/my")
    public List<SubmissionResponse> getMySubmissions(Authentication auth) {
        return submissionService.getMySubmissions(auth.getName());
    }
}