package com.examplatform.modules.question.controller.admin;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.modules.question.dto.request.TagCreateRequest;
import com.examplatform.modules.question.dto.response.TagResponse;
import com.examplatform.modules.question.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TagAdminController {

    private final TagService tagService;

    @GetMapping("/admin/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>>
            getAllTags() {
        return ResponseEntity.ok(
            ApiResponse.success(tagService.getAllTags())
        );
    }

    @PostMapping("/admin/tags")
    public ResponseEntity<ApiResponse<TagResponse>>
            createTag(@Valid @RequestBody TagCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                    "Tag created",
                    tagService.createTag(request)
                ));
    }

    @DeleteMapping("/admin/tags/{id}")
    public ResponseEntity<ApiResponse<Void>>
            deleteTag(@PathVariable String id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(
            ApiResponse.success("Tag deleted", null)
        );
    }
}