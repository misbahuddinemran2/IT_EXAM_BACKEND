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

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class IctQuickReplyService {

private final IctQuickReplyRepository repository;


/*
 * ===================================
 * IN-MEMORY CACHE
 *
 * প্রতিটা entry এখানে আগে থেকেই
 * split + normalize করা থাকে,
 * যাতে findMatch() কলে বারবার
 * এই কাজ করতে না হয়।
 * ===================================
 */

private volatile List<QuickReplyPattern> cachedReplies =
        List.of();


/*
 * ===================================
 * INIT
 * ===================================
 */

@PostConstruct
public void init() {

    refreshCache();
}


/*
 * ===================================
 * CACHE REFRESH
 * ===================================
 */

@Scheduled(fixedRate = 5 * 60 * 1000)
public void refreshCache() {

    try {

        List<QuickReplyPattern> patterns =
                repository.findByIsActiveTrue()
                        .stream()
                        .filter(Objects::nonNull)
                        .flatMap(reply -> {

                            if (reply.getKeywords() == null
                                    || reply.getKeywords().isBlank()) {

                                return Stream.<QuickReplyPattern>empty();
                            }

                            return Arrays.stream(
                                            reply.getKeywords().split(",")
                                    )
                                    .map(this::normalize)
                                    .filter(keyword ->
                                            !keyword.isBlank()
                                    )
                                    .map(keyword ->
                                            new QuickReplyPattern(
                                                    keyword,
                                                    reply.getReplyText()
                                            )
                                    );
                        })
                        /*
                         * Longer phrase আগে ম্যাচ হবে।
                         *
                         * Example:
                         * "system prompt" আগে match হবে
                         * "system"-এর আগে
                         */
                        .sorted(
                                Comparator.comparingInt(
                                        (QuickReplyPattern pattern) ->
                                                pattern.keyword().length()
                                ).reversed()
                        )
                        .toList();


        cachedReplies =
                List.copyOf(patterns);


        log.info(
                "ICT quick-reply cache refreshed. Keywords: {}",
                cachedReplies.size()
        );


    } catch (Exception e) {

        log.error(
                "ICT quick-reply cache refresh failed",
                e
        );
    }
}


/*
 * ===================================
 * QUICK REPLY MATCH
 * ===================================
 */

public Optional<String> findMatch(
        String question
) {


    if (
            question == null
                    || question.isBlank()
    ) {

        return Optional.empty();
    }


    String normalizedQuestion =
            normalize(question);


    for (
            QuickReplyPattern pattern :
            cachedReplies
    ) {


        if (
                isSafeMatch(
                        normalizedQuestion,
                        pattern.keyword()
                )
        ) {


            log.info(
                    "ICT quick reply matched. Keyword: {}",
                    pattern.keyword()
            );


            return Optional.of(
                    pattern.replyText()
            );
        }
    }


    return Optional.empty();
}


/*
 * ===================================
 * SAFE WORD / PHRASE MATCH
 * ===================================
 */

private boolean isSafeMatch(
        String question,
        String keyword
) {


    if (
            question == null
                    || keyword == null
                    || question.isBlank()
                    || keyword.isBlank()
    ) {

        return false;
    }


    /*
     * Exact full question
     */

    if (question.equals(keyword)) {

        return true;
    }


    /*
     * Phrase match
     *
     * Example:
     * keyword: "system prompt"
     * question: "system prompt bolo"
     * → MATCH
     */

    if (keyword.contains(" ")) {

        return question.contains(keyword);
    }


    /*
     * Single word exact match
     *
     * IMPORTANT:
     * keyword "ai" হলে
     * "আমি ai শিখবো" → MATCH
     * "said" এর ভিতরের "ai" → MATCH হবে না
     */

    String[] words =
            question.split(" ");


    for (String word : words) {

        if (word.equals(keyword)) {

            return true;
        }
    }


    return false;
}


/*
 * ===================================
 * NORMALIZE
 * ===================================
 */

private String normalize(
        String text
) {


    if (text == null) {

        return "";
    }


    return Normalizer.normalize(
                    text,
                    Normalizer.Form.NFKC
            )
            .toLowerCase(Locale.ROOT)

            /*
             * বাংলা + English punctuation
             */

            .replaceAll(
                    "[?!.,।:;\"'`()\\[\\]{}<>|/\\\\]",
                    " "
            )

            /*
             * Extra whitespace
             */

            .replaceAll(
                    "\\s+",
                    " "
            )

            .trim();
}


/*
 * ===================================
 * ADMIN CRUD
 * ===================================
 */

public List<IctQuickReplyResponse> getAll() {

    return repository
            .findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
}


public IctQuickReplyResponse create(
        IctQuickReplyRequest request
) {


    validateRequest(request);


    IctQuickReply entity =
            IctQuickReply.builder()
                    .keywords(
                            request.getKeywords().trim()
                    )
                    .replyText(
                            request.getReplyText().trim()
                    )
                    .isActive(
                            request.getIsActive() == null
                                    || request.getIsActive()
                    )
                    .build();


    IctQuickReply saved =
            repository.save(entity);


    refreshCache();


    return toResponse(saved);
}


public IctQuickReplyResponse update(
        String id,
        IctQuickReplyRequest request
) {


    IctQuickReply entity =
            repository.findById(id)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Quick reply পাওয়া যায়নি: "
                                            + id
                    )
                    );


    if (
            request.getKeywords() != null
                    && !request.getKeywords().isBlank()
    ) {

        entity.setKeywords(
                request.getKeywords().trim()
        );
    }


    if (
            request.getReplyText() != null
                    && !request.getReplyText().isBlank()
    ) {

        entity.setReplyText(
                request.getReplyText().trim()
        );
    }


    if (
            request.getIsActive() != null
    ) {

        entity.setActive(
                request.getIsActive()
        );
    }


    IctQuickReply saved =
            repository.save(entity);


    refreshCache();


    return toResponse(saved);
}


public void delete(
        String id
) {


    if (
            !repository.existsById(id)
    ) {

        throw new IllegalArgumentException(
                "Quick reply পাওয়া যায়নি: "
                        + id
        );
    }


    repository.deleteById(id);


    refreshCache();
}


/*
 * ===================================
 * VALIDATION
 * ===================================
 */

private void validateRequest(
        IctQuickReplyRequest request
) {


    if (request == null) {

        throw new IllegalArgumentException(
                "Request খালি হতে পারবে না"
        );
    }


    if (
            request.getKeywords() == null
                    || request.getKeywords().isBlank()
    ) {

        throw new IllegalArgumentException(
                "Keywords খালি হতে পারবে না"
        );
    }


    if (
            request.getReplyText() == null
                    || request.getReplyText().isBlank()
    ) {

        throw new IllegalArgumentException(
                "Reply text খালি হতে পারবে না"
        );
    }
}


/*
 * ===================================
 * RESPONSE MAPPER
 * ===================================
 */

private IctQuickReplyResponse toResponse(
        IctQuickReply entity
) {

    return IctQuickReplyResponse.builder()
            .id(entity.getId())
            .keywords(entity.getKeywords())
            .replyText(entity.getReplyText())
            .isActive(entity.isActive())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
}


/*
 * ===================================
 * QUICK REPLY PATTERN
 * ===================================
 */

private record QuickReplyPattern(
        String keyword,
        String replyText
) {
}

}
