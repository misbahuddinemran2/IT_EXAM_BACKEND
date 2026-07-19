package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.dto.IctIntentKeywordRequest;
import com.examplatform.modules.ictchatbot.dto.IctIntentKeywordResponse;
import com.examplatform.modules.ictchatbot.service.IctIntentKeywordAdminService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/ict/intent-keyword")
@RequiredArgsConstructor
public class IctIntentKeywordAdminController {

    private final IctIntentKeywordAdminService service;


    @GetMapping
    public List<IctIntentKeywordResponse> getAll() {
        return service.getAll();
    }


    @GetMapping("/available-intents")
    public List<String> getAvailableIntents() {
        return service.getAvailableIntents();
    }


    @PostMapping
    public IctIntentKeywordResponse create(@RequestBody IctIntentKeywordRequest request) {
        return service.create(request);
    }


    @PutMapping("/{id}")
    public IctIntentKeywordResponse update(@PathVariable String id, @RequestBody IctIntentKeywordRequest request) {
        return service.update(id, request);
    }


    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
