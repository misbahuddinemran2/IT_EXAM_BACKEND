package com.examplatform.modules.exam.controller;

import com.examplatform.modules.exam.dto.*;
import com.examplatform.modules.exam.service.PracticeExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
@Tag(name = "Exam", description = "Exam Management APIs")
public class ExamController {

    private final PracticeExamService practiceExamService;

    @GetMapping("/free-exam/start")
    @Operation(summary = "Start Free Exam", description = "Start a free 15-mark exam")
    public ResponseEntity<Map<String, Object>> startFreeExam(@RequestParam(required = false) String userId) {
        try {
            var session = practiceExamService.startFreeExam(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("totalQuestions", session.getTotalQuestions());
            response.put("timeLimit", 900);
            response.put("message", "Exam started successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("START-FREE-EXAM ERROR: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{sessionId}/next-question")
    @Operation(summary = "Get Next Question")
    public ResponseEntity<QuestionResponse> getNextQuestion(@PathVariable String sessionId) {
        try {
            QuestionResponse question = practiceExamService.getNextQuestion(sessionId);
            if (question == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            log.error("NEXT-QUESTION ERROR for session {}: ", sessionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{sessionId}/submit-answer")
    @Operation(summary = "Submit Answer")
    public ResponseEntity<AnswerResultResponse> submitAnswer(
            @PathVariable String sessionId,
            @RequestBody SubmitAnswerRequest request) {
        try {
            return ResponseEntity.ok(practiceExamService.submitAnswer(sessionId, request));
        } catch (Exception e) {
            log.error("SUBMIT-ANSWER ERROR for session {}: ", sessionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{sessionId}/progress")
    @Operation(summary = "Get Current Progress")
    public ResponseEntity<Map<String, Object>> getProgress(@PathVariable String sessionId) {
        try {
            var session = practiceExamService.getSessionProgress(sessionId);
            Map<String, Object> response = new HashMap<>();
            response.put("currentQuestion", session.getAttemptedCount() + 1);
            response.put("totalQuestions", session.getTotalQuestions());
            response.put("correctCount", session.getCorrectCount());
            response.put("skippedCount", session.getSkipCount());
            response.put("timeSpentSec", session.getTimeSpentSec());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("PROGRESS ERROR for session {}: ", sessionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<ExamResultResponse> finishExam(@PathVariable String sessionId) {
        try {
            ExamResultResponse result = practiceExamService.finishExam(sessionId);
            log.info("FINISH RESULT: {}", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("FINISH ERROR: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
