package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.dto.IctQuickReplyRequest;
import com.examplatform.modules.ictchatbot.dto.IctQuickReplyResponse;
import com.examplatform.modules.ictchatbot.service.IctQuickReplyService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/ict/quick-reply")
@RequiredArgsConstructor
public class IctQuickReplyController {

    private final IctQuickReplyService quickReplyService;

    @GetMapping
    public ResponseEntity<List<IctQuickReplyResponse>> getAll() {
        return ResponseEntity.ok(quickReplyService.getAll());
    }

    @PostMapping
    public ResponseEntity<IctQuickReplyResponse> create(
            @RequestBody IctQuickReplyRequest request
    ) {
        return ResponseEntity.ok(quickReplyService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IctQuickReplyResponse> update(
            @PathVariable String id,
            @RequestBody IctQuickReplyRequest request
    ) {
        return ResponseEntity.ok(quickReplyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id) {
        quickReplyService.delete(id);
        return ResponseEntity.ok(Map.of("id", id, "deleted", true));
    }
}
