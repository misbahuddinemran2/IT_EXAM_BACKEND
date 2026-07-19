package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.dto.IctRewriteKeywordRequest;
import com.examplatform.modules.ictchatbot.dto.IctRewriteKeywordResponse;
import com.examplatform.modules.ictchatbot.service.IctRewriteKeywordAdminService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/ict/rewrite-keyword")
@RequiredArgsConstructor
public class IctRewriteKeywordAdminController {

    private final IctRewriteKeywordAdminService service;

    @GetMapping
    public List<IctRewriteKeywordResponse> getAll() {
        return service.getAll();
    }

    @PostMapping
    public IctRewriteKeywordResponse create(@RequestBody IctRewriteKeywordRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public IctRewriteKeywordResponse update(
            @PathVariable UUID id,
            @RequestBody IctRewriteKeywordRequest request
    ) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
