package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.dto.IctRewriteRequest;
import com.examplatform.modules.ictchatbot.dto.IctRewriteResponse;
import com.examplatform.modules.ictchatbot.service.IctRewriteService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ict/rewrite")
@RequiredArgsConstructor
public class IctRewriteController {

    private final IctRewriteService rewriteService;

    @PostMapping
    public IctRewriteResponse rewrite(@RequestBody IctRewriteRequest request) {

        return rewriteService.rewrite(
                request.getOriginalAnswer(),
                request.getInstruction()
        );
    }
}
