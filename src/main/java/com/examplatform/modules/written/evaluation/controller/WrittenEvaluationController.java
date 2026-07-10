package com.examplatform.modules.written.evaluation.controller;

import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.evaluation.service.WrittenEvaluationService;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/written/evaluations")
@RequiredArgsConstructor
public class WrittenEvaluationController {

    private final WrittenEvaluationService evaluationService;
    private final WrittenSubmissionRepository submissionRepository;

    // Student views their own evaluation result for a submission
    @GetMapping("/submission/{submissionId}")
    public EvaluationResponse getMyEvaluation(@PathVariable String submissionId, Authentication auth) {
        verifyOwnership(submissionId, auth);
        return evaluationService.getEvaluationBySubmissionId(submissionId);
    }

    /**
     * Ensures the logged-in student owns this submission before letting them view its evaluation.
     * The JWT subject (auth.getName()) is the student's users.id (UUID) directly —
     * unlike admin auth, no username-to-id lookup is needed here.
     */
    private void verifyOwnership(String submissionId, Authentication auth) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        String loggedInUserId = auth.getName();
        if (!submission.getUserId().equals(loggedInUserId)) {
            throw new AccessDeniedException("You are not allowed to view this submission's evaluation");
        }
    }
}
