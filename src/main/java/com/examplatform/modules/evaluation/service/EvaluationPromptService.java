package com.examplatform.modules.evaluation.service;

import com.examplatform.common.exception.DuplicateResourceException;
import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.evaluation.dto.*;
import com.examplatform.modules.evaluation.entity.EvaluationPrompt;
import com.examplatform.modules.evaluation.mapper.EvaluationPromptMapper;
import com.examplatform.modules.evaluation.repository.EvaluationPromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationPromptService {

    private final EvaluationPromptRepository promptRepository;
    private final EvaluationPromptMapper promptMapper;

    @Transactional
    public PromptResponse create(PromptCreateRequest req) {
        if (promptRepository.existsByNameAndVersion(req.getName(), req.getVersion())) {
            throw new DuplicateResourceException(
                    "Prompt already exists: " + req.getName() + " v" + req.getVersion());
        }
        return promptMapper.toResponse(promptRepository.save(promptMapper.toEntity(req)));
    }

    @Transactional
    public PromptResponse update(String id, PromptUpdateRequest req) {
        EvaluationPrompt prompt = getOrThrow(id);
        promptMapper.applyUpdate(prompt, req);
        return promptMapper.toResponse(promptRepository.save(prompt));
    }

    public PromptResponse getById(String id) {
        return promptMapper.toResponse(getOrThrow(id));
    }

    public List<PromptResponse> listAll() {
        return promptRepository.findAll().stream().map(promptMapper::toResponse).toList();
    }

    private EvaluationPrompt getOrThrow(String id) {
        return promptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationPrompt", id));
    }
}
