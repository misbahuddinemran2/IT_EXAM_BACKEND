package com.examplatform.modules.ictchatbot.service;

import com.examplatform.modules.ictchatbot.dto.IctQuickReplyRequest;
import com.examplatform.modules.ictchatbot.dto.IctQuickReplyResponse;
import com.examplatform.modules.ictchatbot.entity.IctQuickReply;
import com.examplatform.modules.ictchatbot.repository.IctQuickReplyRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IctQuickReplyService {

    private final IctQuickReplyRepository repository;

    /*
     * In-memory cache — প্রতি request এ DB hit এড়াতে।
     * প্রতি ৫ মিনিটে refresh হবে (নিচে @Scheduled দ্রষ্টব্য)।
     */
    private volatile List<IctQuickReply> cachedReplies = new ArrayList<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // ৫ মিনিট পরপর
    public void refreshCache() {
        try {
            cachedReplies = repository.findByIsActiveTrue();
            log.info("ICT quick-reply cache refreshed. Entries: {}", cachedReplies.size());
        } catch (Exception e) {
            log.error("ICT quick-reply cache refresh failed", e);
        }
    }

    /**
     * প্রশ্নের সাথে কোনো quick-reply keyword মেলে কিনা চেক করে।
     * মিললে reply text রিটার্ন করে, না মিললে Optional.empty()।
     */
    public Optional<String> findMatch(String question) {

        if (question == null || question.isBlank()) {
            return Optional.empty();
        }

        String normalizedQuestion = normalize(question);

        for (IctQuickReply reply : cachedReplies) {

            String[] keywordList = reply.getKeywords().split(",");

            for (String keyword : keywordList) {

                String normalizedKeyword = normalize(keyword);

                if (normalizedKeyword.isBlank()) {
                    continue;
                }

                if (normalizedQuestion.contains(normalizedKeyword)) {
                    return Optional.of(reply.getReplyText());
                }
            }
        }

        return Optional.empty();
    }

    private String normalize(String text) {
        return text
                .toLowerCase(Locale.ROOT)
                .replaceAll("[?!.,।]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /*
     * ===================================
     * ADMIN CRUD METHODS
     * ===================================
     */

    public List<IctQuickReplyResponse> getAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public IctQuickReplyResponse create(IctQuickReplyRequest request) {

        validateRequest(request);

        IctQuickReply entity = IctQuickReply.builder()
                .keywords(request.getKeywords().trim())
                .replyText(request.getReplyText().trim())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();

        IctQuickReply saved = repository.save(entity);
        refreshCache();

        return toResponse(saved);
    }

    public IctQuickReplyResponse update(String id, IctQuickReplyRequest request) {

        IctQuickReply entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quick reply পাওয়া যায়নি: " + id));

        if (request.getKeywords() != null && !request.getKeywords().isBlank()) {
            entity.setKeywords(request.getKeywords().trim());
        }

        if (request.getReplyText() != null && !request.getReplyText().isBlank()) {
            entity.setReplyText(request.getReplyText().trim());
        }

        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }

        IctQuickReply saved = repository.save(entity);
        refreshCache();

        return toResponse(saved);
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Quick reply পাওয়া যায়নি: " + id);
        }
        repository.deleteById(id);
        refreshCache();
    }

    private void validateRequest(IctQuickReplyRequest request) {
        if (request.getKeywords() == null || request.getKeywords().isBlank()) {
            throw new IllegalArgumentException("Keywords খালি হতে পারবে না");
        }
        if (request.getReplyText() == null || request.getReplyText().isBlank()) {
            throw new IllegalArgumentException("Reply text খালি হতে পারবে না");
        }
    }

    private IctQuickReplyResponse toResponse(IctQuickReply entity) {
        return IctQuickReplyResponse.builder()
                .id(entity.getId())
                .keywords(entity.getKeywords())
                .replyText(entity.getReplyText())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
