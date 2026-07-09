package com.examplatform.modules.written.submission.controller;

import com.examplatform.modules.written.submission.response.SubmissionResponse;
import com.examplatform.modules.written.submission.service.WrittenSubmissionService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/written/submissions")
@RequiredArgsConstructor
public class AdminWrittenSubmissionController {

    private final WrittenSubmissionService submissionService;

    @GetMapping("/exam/{examId}")
    public List<SubmissionResponse> getSubmissionsForExam(@PathVariable String examId) {
        return submissionService.getSubmissionsForExam(examId);
    }
}