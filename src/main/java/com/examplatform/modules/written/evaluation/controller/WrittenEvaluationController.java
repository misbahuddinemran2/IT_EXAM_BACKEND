package com.examplatform.modules.written.evaluation.controller;

import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.evaluation.service.WrittenEvaluationService;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionTranscriptRepository;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/written/evaluations")
@RequiredArgsConstructor
public class WrittenEvaluationController {

    private final WrittenEvaluationService evaluationService;
    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenSubmissionTranscriptRepository transcriptRepository;
    private final WrittenQuestionRepository questionRepository;

    // Student views their own evaluation result for a submission.
    // If written_settings.resultPublishMode = MANUAL and the admin hasn't published this
    // specific result yet, marks/details are withheld — only resultPublished=false and the
    // evaluation status are returned, so the student app can show a "still under review" state.
    @GetMapping("/submission/{submissionId}")
    public EvaluationResponse getMyEvaluation(@PathVariable String submissionId, Authentication auth) {
        verifyOwnership(submissionId, auth);
        EvaluationResponse response = evaluationService.getEvaluationBySubmissionId(submissionId);

        if (!response.isResultPublished()) {
            return EvaluationResponse.builder()
                    .id(response.getId())
                    .submissionId(response.getSubmissionId())
                    .examId(response.getExamId())
                    .studentUserId(response.getStudentUserId())
                    .evaluationMode(response.getEvaluationMode())
                    .status(response.getStatus())
                    .resultPublished(false)
                    .details(java.util.List.of())
                    .createdAt(response.getCreatedAt())
                    .updatedAt(response.getUpdatedAt())
                    .build();
        }

        return response;
    }

    /**
     * Full "answer review" for a student's own result page: for every question+part, returns
     * the question text, the student's own answer, the admin's model answer, obtained/max mark,
     * and feedback — all in one call. Only available once the evaluation is COMPLETED and
     * resultPublished=true (same gate as getMyEvaluation); the model answer must never leak to
     * a student who hasn't seen their published result yet.
     */
    @GetMapping("/submission/{submissionId}/answer-review")
    public List<Map<String, Object>> getAnswerReview(@PathVariable String submissionId, Authentication auth) {
        verifyOwnership(submissionId, auth);
        EvaluationResponse evaluation = evaluationService.getEvaluationBySubmissionId(submissionId);

        if (!evaluation.isResultPublished() || !"COMPLETED".equals(evaluation.getStatus())) {
            throw new AccessDeniedException("Result not published yet for this submission");
        }

        List<com.examplatform.modules.written.submission.entity.WrittenSubmissionTranscript> transcripts =
                transcriptRepository.findBySubmissionId(submissionId);
        Map<String, String> answerMap = new java.util.HashMap<>();
        for (var t : transcripts) {
            answerMap.put(t.getQuestion().getId() + "::" + t.getPart().name(), t.getTranscribedText());
        }

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (var d : evaluation.getDetails()) {
            com.examplatform.modules.written.question.entity.WrittenQuestion question =
                    questionRepository.findById(d.getQuestionId()).orElse(null);
            if (question == null) continue;

            String part = d.getPart();
            String questionText = switch (part) {
                case "A" -> question.getPartAQuestion();
                case "B" -> question.getPartBQuestion();
                case "C" -> question.getPartCQuestion();
                case "D" -> question.getPartDQuestion();
                default -> null;
            };
            String modelAnswer = switch (part) {
                case "A" -> question.getPartAModelAnswer();
                case "B" -> question.getPartBModelAnswer();
                case "C" -> question.getPartCModelAnswer();
                case "D" -> question.getPartDModelAnswer();
                default -> null;
            };

            Map<String, Object> item = new java.util.HashMap<>();
            item.put("questionId", d.getQuestionId());
            item.put("questionOrder", d.getQuestionOrder());
            item.put("part", part);
            item.put("questionText", questionText);
            item.put("studentAnswer", answerMap.get(d.getQuestionId() + "::" + part));
            item.put("modelAnswer", modelAnswer);
            item.put("obtainedMark", d.getObtainedMark());
            item.put("maxMark", d.getMaxMark());
            item.put("feedback", d.getFeedback());
            result.add(item);
        }
        return result;
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
