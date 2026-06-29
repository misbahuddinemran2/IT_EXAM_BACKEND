package com.examplatform.modules.exam.controller;

import com.examplatform.modules.exam.dto.request.*;
import com.examplatform.modules.exam.dto.response.*;
import com.examplatform.modules.exam.service.ExamAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/exams")
@RequiredArgsConstructor
public class ExamAdminController {

    private final ExamAdminService examAdminService;

    // ============================================
    // CREATE EXAM
    // POST /api/v1/admin/exams/create
    // ============================================
    @PostMapping("/create")
    public ResponseEntity<?> createExam(
            @RequestBody ExamCreationRequest request,
            @RequestHeader("X-Admin-Id") String adminId) {
        try {
            ExamResponse response = examAdminService.createExam(request, adminId);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "success", true,
                            "message", "Exam created successfully",
                            "data", response
                    )
            );
        } catch (Exception e) {
            log.error("Error creating exam: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // UPDATE EXAM (শুধু DRAFT)
    // PUT /api/v1/admin/exams/{examId}
    // ============================================
    @PutMapping("/{examId}")
    public ResponseEntity<?> updateExam(
            @PathVariable String examId,
            @RequestBody ExamCreationRequest request) {
        try {
            ExamResponse response = examAdminService.updateExam(examId, request);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Exam updated successfully",
                            "data", response
                    )
            );
        } catch (Exception e) {
            log.error("Error updating exam {}: {}", examId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // PUBLISH EXAM
    // POST /api/v1/admin/exams/{examId}/publish
    // ============================================
    @PostMapping("/{examId}/publish")
    public ResponseEntity<?> publishExam(@PathVariable String examId) {
        try {
            ExamResponse response = examAdminService.publishExam(examId);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Exam published successfully",
                            "data", response
                    )
            );
        } catch (Exception e) {
            log.error("Error publishing exam {}: {}", examId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // ARCHIVE EXAM
    // POST /api/v1/admin/exams/{examId}/archive
    // ============================================
    @PostMapping("/{examId}/archive")
    public ResponseEntity<?> archiveExam(@PathVariable String examId) {
        try {
            ExamResponse response = examAdminService.archiveExam(examId);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Exam archived successfully",
                            "data", response
                    )
            );
        } catch (Exception e) {
            log.error("Error archiving exam {}: {}", examId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // DELETE EXAM (শুধু DRAFT)
    // DELETE /api/v1/admin/exams/{examId}
    // ============================================
    @DeleteMapping("/{examId}")
    public ResponseEntity<?> deleteExam(@PathVariable String examId) {
        try {
            examAdminService.deleteExam(examId);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Exam deleted successfully"
                    )
            );
        } catch (Exception e) {
            log.error("Error deleting exam {}: {}", examId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // GET ALL EXAMS LIST
    // GET /api/v1/admin/exams/list
    // ============================================
    @GetMapping("/list")
    public ResponseEntity<?> getAllExams() {
        try {
            List<ExamListResponse> exams = examAdminService.getAllExams();
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", exams,
                            "total", exams.size()
                    )
            );
        } catch (Exception e) {
            log.error("Error fetching exam list: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // GET EXAMS BY TYPE
    // GET /api/v1/admin/exams/list?type=DAILY
    // ============================================
    @GetMapping("/list/type/{examType}")
    public ResponseEntity<?> getExamsByType(@PathVariable String examType) {
        try {
            List<ExamListResponse> exams =
                    examAdminService.getExamsByType(examType);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", exams,
                            "total", exams.size()
                    )
            );
        } catch (Exception e) {
            log.error("Error fetching exams by type {}: {}", examType, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // GET EXAM DETAIL (Admin)
    // GET /api/v1/admin/exams/{examId}
    // ============================================
    @GetMapping("/{examId}")
    public ResponseEntity<?> getExamDetail(@PathVariable String examId) {
        try {
            ExamResponse response = examAdminService.getExamDetail(examId);
            return ResponseEntity.ok(
                    Map.of("success", true, "data", response)
            );
        } catch (Exception e) {
            log.error("Error fetching exam {}: {}", examId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // ADD QUESTIONS MANUALLY
    // POST /api/v1/admin/exams/{examId}/questions/add
    // ============================================
    @PostMapping("/{examId}/questions/add")
    public ResponseEntity<?> addQuestionsManually(
            @PathVariable String examId,
            @RequestBody AddQuestionsRequest request) {
        try {
            examAdminService.addQuestionsManually(examId, request);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Questions added successfully"
                    )
            );
        } catch (Exception e) {
            log.error("Error adding questions to exam {}: {}", examId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // REGENERATE QUESTIONS
    // POST /api/v1/admin/exams/{examId}/questions/regenerate
    // ============================================
    @PostMapping("/{examId}/questions/regenerate")
    public ResponseEntity<?> regenerateQuestions(@PathVariable String examId) {
        try {
            ExamResponse response = examAdminService.regenerateQuestions(examId);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Questions regenerated successfully",
                            "data", response
                    )
            );
        } catch (Exception e) {
            log.error("Error regenerating questions for exam {}: {}",
                    examId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // EXAM STATS
    // GET /api/v1/admin/exams/stats
    // ============================================
    @GetMapping("/stats")
    public ResponseEntity<?> getExamStats() {
        try {
            ExamStatsResponse stats = examAdminService.getExamStats();
            return ResponseEntity.ok(
                    Map.of("success", true, "data", stats)
            );
        } catch (Exception e) {
            log.error("Error fetching exam stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }
}