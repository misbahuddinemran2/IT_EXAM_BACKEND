package com.examplatform.modules.question.controller.admin;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.modules.question.dto.request.ConceptCreateRequest;
import com.examplatform.modules.question.dto.response.ConceptResponse;
import com.examplatform.modules.question.service.ConceptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConceptAdminController {

    private final ConceptService conceptService;

    @GetMapping("/admin/concepts")
    public ResponseEntity<ApiResponse<List<ConceptResponse>>>
            getConcepts(
                @RequestParam(required = false) String topicId,
                @RequestParam(required = false) String search) {

        List<ConceptResponse> result;
        if (search != null && !search.isBlank()) {
            result = conceptService.searchConcepts(search);
        } else if (topicId != null) {
            result = conceptService.getConceptsByTopic(topicId);
        } else {
            result = List.of();
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/admin/concepts")
    public ResponseEntity<ApiResponse<ConceptResponse>>
            createConcept(
                @Valid @RequestBody ConceptCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                    "Concept created",
                    conceptService.createConcept(request, "system")
                ));
    }

    @DeleteMapping("/admin/concepts/{id}")
    public ResponseEntity<ApiResponse<Void>>
            deleteConcept(@PathVariable String id) {
        conceptService.deleteConcept(id);
        return ResponseEntity.ok(
            ApiResponse.success("Concept deleted", null)
        );
    }
}