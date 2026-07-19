package com.examplatform.modules.ictchatbot.service;

import com.examplatform.modules.ictchatbot.dto.IctIntentKeywordRequest;
import com.examplatform.modules.ictchatbot.dto.IctIntentKeywordResponse;
import com.examplatform.modules.ictchatbot.entity.IctIntentKeyword;
import com.examplatform.modules.ictchatbot.repository.IctIntentKeywordRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IctIntentKeywordAdminService {

    private final IctIntentKeywordRepository repository;
    private final IctIntentDetectorService intentDetectorService;


    public List<IctIntentKeywordResponse> getAll() {

        return repository.findAllByOrderByIntentAscCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    public List<String> getAvailableIntents() {

        return java.util.Arrays.stream(IctIntentDetectorService.Intent.values())
                .filter(i -> i != IctIntentDetectorService.Intent.UNKNOWN)
                .map(Enum::name)
                .toList();
    }


    public IctIntentKeywordResponse create(IctIntentKeywordRequest request) {

        validateRequest(request);

        IctIntentKeyword entity = IctIntentKeyword.builder()
                .intent(request.getIntent().trim().toUpperCase())
                .keyword(request.getKeyword().trim())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();

        IctIntentKeyword saved = repository.save(entity);

        intentDetectorService.refreshCache();

        return toResponse(saved);
    }


    public IctIntentKeywordResponse update(String id, IctIntentKeywordRequest request) {

        IctIntentKeyword entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Intent keyword পাওয়া যায়নি: " + id));

        if (request.getIntent() != null && !request.getIntent().isBlank()) {
            validateIntent(request.getIntent());
            entity.setIntent(request.getIntent().trim().toUpperCase());
        }

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            entity.setKeyword(request.getKeyword().trim());
        }

        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        IctIntentKeyword saved = repository.save(entity);

        intentDetectorService.refreshCache();

        return toResponse(saved);
    }


    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Intent keyword পাওয়া যায়নি: " + id);
        }

        repository.deleteById(id);

        intentDetectorService.refreshCache();
    }


    private void validateRequest(IctIntentKeywordRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request খালি হতে পারবে না");
        }

        if (request.getIntent() == null || request.getIntent().isBlank()) {
            throw new IllegalArgumentException("Intent খালি হতে পারবে না");
        }

        validateIntent(request.getIntent());

        if (request.getKeyword() == null || request.getKeyword().isBlank()) {
            throw new IllegalArgumentException("Keyword খালি হতে পারবে না");
        }
    }


    private void validateIntent(String intent) {

        try {
            IctIntentDetectorService.Intent parsed =
                    IctIntentDetectorService.Intent.valueOf(intent.trim().toUpperCase());

            if (parsed == IctIntentDetectorService.Intent.UNKNOWN) {
                throw new IllegalArgumentException("UNKNOWN intent সেট করা যাবে না");
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid intent: " + intent);
        }
    }


    private IctIntentKeywordResponse toResponse(IctIntentKeyword entity) {

        return IctIntentKeywordResponse.builder()
                .id(entity.getId())
                .intent(entity.getIntent())
                .keyword(entity.getKeyword())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
