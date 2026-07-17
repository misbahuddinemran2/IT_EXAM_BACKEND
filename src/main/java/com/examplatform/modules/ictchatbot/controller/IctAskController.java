package com.examplatform.modules.ictchatbot.controller;

import com.examplatform.modules.ictchatbot.dto.IctAskRequest;
import com.examplatform.modules.ictchatbot.dto.IctAskResponse;
import com.examplatform.modules.ictchatbot.service.IctAskService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ict")
@RequiredArgsConstructor
public class IctAskController {

    private final IctAskService askService;

    @PostMapping("/ask")
    public ResponseEntity<IctAskResponse> ask(@RequestBody IctAskRequest request) {
        IctAskResponse response = askService.ask(request.getQuestion());
        return ResponseEntity.ok(response);
    }
}
