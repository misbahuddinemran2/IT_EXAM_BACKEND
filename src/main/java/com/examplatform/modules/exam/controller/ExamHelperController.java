package com.examplatform.modules.exam.controller;

import com.examplatform.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/exam")
@RequiredArgsConstructor
@Tag(name = "Exam Helper", description = "Subject, Topic, Chapter List")
public class ExamHelperController {

    private final JdbcTemplate jdbcTemplate;

    // ─── Subject List ──────────────────────────────────────────
    @GetMapping("/subjects")
    @Operation(summary = "সব Subject দেখা")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSubjects() {
        List<Map<String, Object>> subjects = jdbcTemplate.queryForList(
                "SELECT id, name, name_bn, code FROM subjects WHERE is_active = 1");
        return ResponseEntity.ok(
                ApiResponse.success("Subjects পাওয়া গেছে", subjects));
    }

    // ─── Topic List by Subject ─────────────────────────────────
    @GetMapping("/subjects/{subjectId}/topics")
    @Operation(summary = "Subject অনুযায়ী Topic দেখা")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopics(
            @PathVariable String subjectId) {
        List<Map<String, Object>> topics = jdbcTemplate.queryForList(
                "SELECT t.id, t.name, t.name_bn FROM topics t " +
                "JOIN chapters c ON t.chapter_id = c.id " +
                "WHERE c.subject_id = ? AND t.is_active = 1 " +
                "ORDER BY t.order_index",
                subjectId);
        return ResponseEntity.ok(
                ApiResponse.success("Topics পাওয়া গেছে", topics));
    }

    // ─── Chapter List by Subject ───────────────────────────────
    @GetMapping("/subjects/{subjectId}/chapters")
    @Operation(summary = "Subject অনুযায়ী Chapter দেখা")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getChapters(
            @PathVariable String subjectId) {
        List<Map<String, Object>> chapters = jdbcTemplate.queryForList(
                "SELECT id, name, name_bn FROM chapters " +
                "WHERE subject_id = ? AND is_active = 1 " +
                "ORDER BY order_index",
                subjectId);
        return ResponseEntity.ok(
                ApiResponse.success("Chapters পাওয়া গেছে", chapters));
    }
}