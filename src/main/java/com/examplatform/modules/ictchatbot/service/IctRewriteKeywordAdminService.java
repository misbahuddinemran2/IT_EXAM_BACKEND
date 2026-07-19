package com.examplatform.modules.ictchatbot.service;

import com.examplatform.modules.ictchatbot.dto.IctRewriteKeywordRequest;
import com.examplatform.modules.ictchatbot.dto.IctRewriteKeywordResponse;
import com.examplatform.modules.ictchatbot.entity.IctRewriteKeyword;
import com.examplatform.modules.ictchatbot.repository.IctRewriteKeywordRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IctRewriteKeywordAdminService {

    private final IctRewriteKeywordRepository repository;
    private final IctRewriteService rewriteService;

    public List<IctRewriteKeywordResponse> getAll() {

        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public IctRewriteKeywordResponse create(IctRewriteKeywordRequest request) {

        IctRewriteKeyword entity = IctRewriteKeyword.builder()
                .category(request.getCategory())
                .keywords(request.getKeywords())
                .isActive(
                        request.getIsActive() != null
                                ? request.getIsActive()
                                : true
                )
                .build();

        IctRewriteKeyword saved = repository.save(entity);

        rewriteService.refreshCache();

        return toResponse(saved);
    }

    public IctRewriteKeywordResponse update(UUID id, IctRewriteKeywordRequest request) {

        IctRewriteKeyword entity = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Rewrite keyword পাওয়া যায়নি")
                );

        if (request.getCategory() != null) {
            entity.setCategory(request.getCategory());
        }

        if (request.getKeywords() != null) {
            entity.setKeywords(request.getKeywords());
        }

        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }

        IctRewriteKeyword saved = repository.save(entity);

        rewriteService.refreshCache();

        return toResponse(saved);
    }

    public void delete(UUID id) {

        repository.deleteById(id);

        rewriteService.refreshCache();
    }

    private IctRewriteKeywordResponse toResponse(IctRewriteKeyword entity) {

        return IctRewriteKeywordResponse.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .keywords(entity.getKeywords())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
