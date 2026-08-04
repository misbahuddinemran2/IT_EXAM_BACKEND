package com.examplatform.modules.evaluation.mapper;

import com.examplatform.modules.evaluation.dto.PromptCreateRequest;
import com.examplatform.modules.evaluation.dto.PromptResponse;
import com.examplatform.modules.evaluation.dto.PromptUpdateRequest;
import com.examplatform.modules.evaluation.entity.EvaluationPrompt;
import org.springframework.stereotype.Component;

@Component
public class EvaluationPromptMapper {

    public EvaluationPrompt toEntity(PromptCreateRequest req) {
        return EvaluationPrompt.builder()
                .name(req.getName())
                .version(req.getVersion())
                .systemPrompt(req.getSystemPrompt())
                .userPrompt(req.getUserPrompt())
                .notes(req.getNotes())
                .build();
    }

    public void applyUpdate(EvaluationPrompt prompt, PromptUpdateRequest req) {
        if (req.getSystemPrompt() != null) prompt.setSystemPrompt(req.getSystemPrompt());
        if (req.getUserPrompt() != null) prompt.setUserPrompt(req.getUserPrompt());
        if (req.getNotes() != null) prompt.setNotes(req.getNotes());
    }

    public PromptResponse toResponse(EvaluationPrompt prompt) {
        return PromptResponse.builder()
                .id(prompt.getId())
                .name(prompt.getName())
                .version(prompt.getVersion())
                .systemPrompt(prompt.getSystemPrompt())
                .userPrompt(prompt.getUserPrompt())
                .notes(prompt.getNotes())
                .createdAt(prompt.getCreatedAt())
                .updatedAt(prompt.getUpdatedAt())
                .build();
    }
}
