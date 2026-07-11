package com.examplatform.modules.written.evaluation.service;

import com.examplatform.modules.written.evaluation.entity.WrittenEvaluation;
import com.examplatform.modules.written.evaluation.entity.WrittenEvaluationDetail;
import com.examplatform.modules.written.evaluation.enums.EvaluationStatus;
import com.examplatform.modules.written.evaluation.manual.request.ManualEvaluationRequest;
import com.examplatform.modules.written.evaluation.manual.request.PartMarkRequest;
import com.examplatform.modules.written.evaluation.mapper.WrittenEvaluationMapper;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationDetailRepository;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationRepository;
import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.enums.QuestionPart;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.settings.repository.WrittenSettingsRepository;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.enums.SubmissionStatus;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * Mode-aware finalization of an evaluation (works for MANUAL, AI, and HYBRID exams).
 * Unlike WrittenManualEvaluationService (which always hardcodes evaluationMode=MANUAL and
 * wipes prior detail rows), this service:
 *  - Uses the exam's actual evaluationMode
 *  - Updates existing WrittenEvaluationDetail rows in place, preserving predicted_mark_manual/ai
 *    and match_score_manual/ai history (set earlier by EvaluationOrchestrationService) instead of
 *    deleting them
 *  - Lets the admin submit the final obtainedMark for every part (whether it came from reviewing
 *    a predicted AI mark, or from reading the answer script manually for a MANUAL-mode part)
 */
@Service
@RequiredArgsConstructor
public class WrittenEvaluationFinalizeService {

    private final WrittenEvaluationRepository evaluationRepository;
    private final WrittenEvaluationDetailRepository detailRepository;
    private final WrittenEvaluationMapper evaluationMapper;
    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenQuestionRepository questionRepository;
    private final WrittenExamRepository examRepository;
    private final WrittenSettingsRepository settingsRepository;

    private static final String SETTINGS_ID = "default";

    @Transactional
    public EvaluationResponse finalizeEvaluation(String submissionId, ManualEvaluationRequest request, String adminId) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        if (submission.getStatus() != SubmissionStatus.SUBMITTED
                && submission.getStatus() != SubmissionStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Submission must be SUBMITTED or UNDER_REVIEW to finalize evaluation");
        }

        WrittenExam exam = examRepository.findById(submission.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + submission.getExamId()));

        WrittenEvaluation evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseGet(() -> WrittenEvaluation.builder()
                        .submission(submission)
                        .evaluationMode(exam.getEvaluationMode())
                        .status(EvaluationStatus.PENDING)
                        .build());

        // Keep evaluationMode in sync with the exam's actual mode (in case it was created earlier
        // by the orchestration service or is being finalized for the first time here)
        evaluation.setEvaluationMode(exam.getEvaluationMode());

        WrittenEvaluation savedEvaluation = evaluationRepository.save(evaluation);

        BigDecimal totalMark = BigDecimal.ZERO;

        for (PartMarkRequest partMark : request.getPartMarks()) {
            WrittenQuestion question = questionRepository.findById(partMark.getQuestionId())
                    .orElseThrow(() -> new NoSuchElementException("Question not found: " + partMark.getQuestionId()));

            QuestionPart part = QuestionPart.valueOf(partMark.getPart());
            BigDecimal maxMark = resolveMaxMark(question, part);

            if (partMark.getObtainedMark().compareTo(maxMark) > 0) {
                throw new IllegalArgumentException("obtainedMark exceeds maxMark for question "
                        + question.getId() + " part " + part);
            }
            if (partMark.getObtainedMark().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("obtainedMark cannot be negative");
            }

            // Find existing detail row (may already have predicted marks from orchestration step)
            // or create a fresh one if this part was never touched before (e.g. pure MANUAL exam).
            WrittenEvaluationDetail detail = detailRepository.findByEvaluationId(savedEvaluation.getId()).stream()
                    .filter(d -> d.getQuestion().getId().equals(question.getId()) && d.getPart() == part)
                    .findFirst()
                    .orElse(WrittenEvaluationDetail.builder()
                            .evaluation(savedEvaluation)
                            .question(question)
                            .part(part)
                            .build());

            detail.setObtainedMark(partMark.getObtainedMark());
            detail.setMaxMark(maxMark);
            if (partMark.getFeedback() != null) {
                detail.setFeedback(partMark.getFeedback());
            }

            detailRepository.save(detail);
            totalMark = totalMark.add(partMark.getObtainedMark());
        }

        savedEvaluation.setTotalMark(totalMark);
        savedEvaluation.setStatus(EvaluationStatus.COMPLETED);
        savedEvaluation.setEvaluatedByAdminId(adminId);
        savedEvaluation.setEvaluatedAt(LocalDateTime.now());

        // written_settings.resultPublishMode decides whether the student can see this mark
        // right away (INSTANT) or only after an admin explicitly publishes it later (MANUAL).
        String resultPublishMode = settingsRepository.findById(SETTINGS_ID)
                .map(s -> s.getResultPublishMode())
                .orElse("MANUAL");
        if ("INSTANT".equalsIgnoreCase(resultPublishMode)) {
            savedEvaluation.setResultPublished(true);
        }

        evaluationRepository.save(savedEvaluation);

        submission.setTotalObtainedMark(totalMark);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submissionRepository.save(submission);

        return evaluationMapper.toResponse(savedEvaluation, detailRepository.findByEvaluationId(savedEvaluation.getId()));
    }

    private BigDecimal resolveMaxMark(WrittenQuestion question, QuestionPart part) {
        return switch (part) {
            case A -> question.getPartAMaxMark();
            case B -> question.getPartBMaxMark();
            case C -> question.getPartCMaxMark();
            case D -> question.getPartDMaxMark();
        };
    }

    /**
     * Manually reveals an already-finalized evaluation's mark to the student.
     * Used when written_settings.resultPublishMode = MANUAL (finalize alone doesn't publish).
     */
    @Transactional
    public EvaluationResponse publishResult(String submissionId) {
        WrittenEvaluation evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Evaluation not found for submission: " + submissionId));

        if (evaluation.getStatus() != EvaluationStatus.COMPLETED) {
            throw new IllegalStateException("Evaluation must be finalized (COMPLETED) before it can be published");
        }

        evaluation.setResultPublished(true);
        evaluationRepository.save(evaluation);

        return evaluationMapper.toResponse(evaluation, detailRepository.findByEvaluationId(evaluation.getId()));
    }
}
