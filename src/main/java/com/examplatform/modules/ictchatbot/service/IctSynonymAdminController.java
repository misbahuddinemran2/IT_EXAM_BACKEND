package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.dto.IctSynonymRequest;
import com.examplatform.modules.ictchatbot.dto.IctSynonymResponse;
import com.examplatform.modules.ictchatbot.service.IctSynonymAdminService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/ict/synonym")
@RequiredArgsConstructor
public class IctSynonymAdminController {

    private final IctSynonymAdminService service;

    @GetMapping
    public List<IctSynonymResponse> getAll() {
        return service.getAll();
    }

    @PostMapping
    public IctSynonymResponse create(@RequestBody IctSynonymRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public IctSynonymResponse update(@PathVariable UUID id, @RequestBody IctSynonymRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
