package com.examplatform.modules.written.evaluation.ai.controller;

import com.examplatform.modules.written.evaluation.ai.request.ManualTranscriptBulkRequest;
import com.examplatform.modules.written.evaluation.ai.request.ManualTranscriptEntryRequest;
import com.examplatform.modules.written.evaluation.ai.service.TranscriptionOrchestrationService;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.enums.QuestionPart;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.entity.WrittenSubmissionTranscript;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionTranscriptRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin/written/submissions/{submissionId}/transcript")
@RequiredArgsConstructor
public class AdminTranscriptionController {

    private final TranscriptionOrchestrationService transcriptionOrchestrationService;
    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenSubmissionTranscriptRepository transcriptRepository;
    private final WrittenQuestionRepository questionRepository;

    /**
     * Admin explicitly triggers AI transcription for a PDF/IMAGE submission.
     * Transcribes ALL AI-mode question+parts for the exam this submission belongs to.
     */
    @PostMapping("/ai")
    public String triggerAiTranscription(@PathVariable String submissionId) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        List<WrittenQuestion> questions = questionRepository.findByExamIdOrderByQuestionOrderAsc(submission.getExamId());

        Map<String, List<QuestionPart>> partsToTranscribe = new HashMap<>();
        for (WrittenQuestion q : questions) {
            partsToTranscribe.put(q.getId(), List.of(QuestionPart.A, QuestionPart.B, QuestionPart.C, QuestionPart.D));
        }

        transcriptionOrchestrationService.ensureTranscribed(submission.getExamId(), submissionId, partsToTranscribe);
        return "Transcription completed for submission: " + submissionId;
    }

    /**
     * Admin manually enters/pastes the transcribed text themselves (no AI call),
     * after extracting it some other way from the PDF/image.
     */
    @PostMapping("/manual")
    public String submitManualTranscript(@PathVariable String submissionId,
                                          @Valid @RequestBody ManualTranscriptBulkRequest request) {

        if (!submissionRepository.existsById(submissionId)) {
            throw new NoSuchElementException("Submission not found: " + submissionId);
        }

        for (ManualTranscriptEntryRequest entry : request.getEntries()) {
            WrittenQuestion question = questionRepository.findById(entry.getQuestionId())
                    .orElseThrow(() -> new NoSuchElementException("Question not found: " + entry.getQuestionId()));

            QuestionPart part = QuestionPart.valueOf(entry.getPart());

            WrittenSubmissionTranscript transcript = transcriptRepository
                    .findBySubmissionIdAndQuestionIdAndPart(submissionId, question.getId(), part)
                    .orElse(WrittenSubmissionTranscript.builder()
                            .submissionId(submissionId)
                            .question(question)
                            .part(part)
                            .build());

            transcript.setTranscribedText(entry.getTranscribedText());
            transcriptRepository.save(transcript);
        }

        return "Manual transcript saved for submission: " + submissionId;
    }
}
