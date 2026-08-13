package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.service.IctVectorizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ict/re-embed")
@RequiredArgsConstructor
public class IctReEmbedAdminController {

    private final IctVectorizeService vectorizeService;

    @PostMapping("/all-chunks")
    @PreAuthorize("hasRole('ADMIN')")
    public String reEmbedAllChunks() {
        int count = vectorizeService.reEmbedAllChunks();
        return "Re-embedded " + count + " chunks successfully.";
    }
}
