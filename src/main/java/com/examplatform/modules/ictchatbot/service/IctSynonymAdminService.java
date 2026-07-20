package com.examplatform.modules.ictchatbot.service;

import com.examplatform.modules.ictchatbot.dto.IctSynonymRequest;
import com.examplatform.modules.ictchatbot.dto.IctSynonymResponse;
import com.examplatform.modules.ictchatbot.entity.IctSynonym;
import com.examplatform.modules.ictchatbot.repository.IctSynonymRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IctSynonymAdminService {

    private final IctSynonymRepository repository;
    private final IctQuickReplyService quickReplyService;

    public List<IctSynonymResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public IctSynonymResponse create(IctSynonymRequest request) {

        if (request.getWord() == null || request.getWord().isBlank()) {
            throw new IllegalArgumentException("Word খালি হতে পারবে না");
        }

        if (request.getCanonicalWord() == null || request.getCanonicalWord().isBlank()) {
            throw new IllegalArgumentException("Canonical word খালি হতে পারবে না");
        }

        IctSynonym entity = IctSynonym.builder()
                .word(request.getWord().trim())
                .canonicalWord(request.getCanonicalWord().trim())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();

        IctSynonym saved = repository.save(entity);

        quickReplyService.refreshCache();

        return toResponse(saved);
    }

    public IctSynonymResponse update(UUID id, IctSynonymRequest request) {

        IctSynonym entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Synonym পাওয়া যায়নি: " + id));

        if (request.getWord() != null && !request.getWord().isBlank()) {
            entity.setWord(request.getWord().trim());
        }

        if (request.getCanonicalWord() != null && !request.getCanonicalWord().isBlank()) {
            entity.setCanonicalWord(request.getCanonicalWord().trim());
        }

        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }

        IctSynonym saved = repository.save(entity);

        quickReplyService.refreshCache();

        return toResponse(saved);
    }

    public void delete(UUID id) {

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Synonym পাওয়া যায়নি: " + id);
        }

        repository.deleteById(id);

        quickReplyService.refreshCache();
    }

    private IctSynonymResponse toResponse(IctSynonym entity) {
        return IctSynonymResponse.builder()
                .id(entity.getId())
                .word(entity.getWord())
                .canonicalWord(entity.getCanonicalWord())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
