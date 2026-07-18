package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.dto.IctAskRequest;
import com.examplatform.modules.ictchatbot.dto.IctAskResponse;
import com.examplatform.modules.ictchatbot.service.IctAskService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ict")
@RequiredArgsConstructor
public class IctAskController {

    private final IctAskService askService;

    @PostMapping("/ask")
    public ResponseEntity<IctAskResponse> ask(
            @RequestBody IctAskRequest request,
            Authentication authentication
    ) {
        // /ict/ask এখন authenticated() হওয়ায় authentication কখনো null হবে না
        String userId = authentication.getName();

        IctAskResponse response = askService.ask(request.getQuestion(), userId);
        return ResponseEntity.ok(response);
    }
}
