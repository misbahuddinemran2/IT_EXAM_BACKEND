package com.examplatform.modules.taxonomy.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.modules.taxonomy.dto.request.*;
import com.examplatform.modules.taxonomy.dto.response.*;
import com.examplatform.modules.taxonomy.service.TaxonomyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TaxonomyController {

    private final TaxonomyService taxonomyService;

    // ─── SUBJECT ───────────────────────────────────────

    @GetMapping("/pub/subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>>
            getAllSubjects() {
        return ResponseEntity.ok(
            ApiResponse.success(taxonomyService.getAllSubjects())
        );
    }

    @GetMapping("/pub/subjects/hierarchy")
    public ResponseEntity<ApiResponse<List<SubjectHierarchyResponse>>>
            getHierarchy() {
        return ResponseEntity.ok(
            ApiResponse.success(taxonomyService.getFullHierarchy())
        );
    }

    @PostMapping("/admin/subjects")
    public ResponseEntity<ApiResponse<SubjectResponse>>
            createSubject(
                @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                    "Subject created",
                    taxonomyService.createSubject(request)
                ));
    }

    @PutMapping("/admin/subjects/{id}")
    public ResponseEntity<ApiResponse<SubjectResponse>>
            updateSubject(
                @PathVariable String id,
                @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Subject updated",
                taxonomyService.updateSubject(id, request)
            )
        );
    }

    @DeleteMapping("/admin/subjects/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @PathVariable String id) {
        taxonomyService.deleteSubject(id);
        return ResponseEntity.ok(
            ApiResponse.success("Subject deleted", null)
        );
    }

    // ─── CHAPTER ───────────────────────────────────────

    @GetMapping("/pub/subjects/{subjectId}/chapters")
    public ResponseEntity<ApiResponse<List<ChapterResponse>>>
            getChapters(@PathVariable String subjectId) {
        return ResponseEntity.ok(
            ApiResponse.success(
                taxonomyService.getChaptersBySubject(subjectId)
            )
        );
    }

    @PostMapping("/admin/chapters")
    public ResponseEntity<ApiResponse<ChapterResponse>>
            createChapter(
                @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                    "Chapter created",
                    taxonomyService.createChapter(request)
                ));
    }

    @PutMapping("/admin/chapters/{id}")
    public ResponseEntity<ApiResponse<ChapterResponse>>
            updateChapter(
                @PathVariable String id,
                @Valid @RequestBody ChapterRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Chapter updated",
                taxonomyService.updateChapter(id, request)
            )
        );
    }

    @DeleteMapping("/admin/chapters/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteChapter(
            @PathVariable String id) {
        taxonomyService.deleteChapter(id);
        return ResponseEntity.ok(
            ApiResponse.success("Chapter deleted", null)
        );
    }

    // ─── TOPIC ─────────────────────────────────────────

    @GetMapping("/pub/chapters/{chapterId}/topics")
    public ResponseEntity<ApiResponse<List<TopicResponse>>>
            getTopics(@PathVariable String chapterId) {
        return ResponseEntity.ok(
            ApiResponse.success(
                taxonomyService.getTopicsByChapter(chapterId)
            )
        );
    }

    @PostMapping("/admin/topics")
    public ResponseEntity<ApiResponse<TopicResponse>>
            createTopic(
                @Valid @RequestBody TopicRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                    "Topic created",
                    taxonomyService.createTopic(request)
                ));
    }

    @PutMapping("/admin/topics/{id}")
    public ResponseEntity<ApiResponse<TopicResponse>>
            updateTopic(
                @PathVariable String id,
                @Valid @RequestBody TopicRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Topic updated",
                taxonomyService.updateTopic(id, request)
            )
        );
    }

    @DeleteMapping("/admin/topics/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(
            @PathVariable String id) {
        taxonomyService.deleteTopic(id);
        return ResponseEntity.ok(
            ApiResponse.success("Topic deleted", null)
        );
    }
}