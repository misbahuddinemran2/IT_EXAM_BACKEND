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

@Service
@RequiredArgsConstructor
@Slf4j
public class IctQuickReplyService {

private final IctQuickReplyRepository repository;

/*
 * ===================================
 * IN-MEMORY CACHE
 * ===================================
 */

private volatile List<IctQuickReply> cachedReplies =
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

        List<IctQuickReply> replies =
                repository.findByIsActiveTrue()
                        .stream()
                        /*
                         * Longer keyword first
                         *
                         * Example:
                         * system prompt
                         * আগে match হবে
                         * system-এর আগে
                         */
                        .sorted(
                                Comparator.comparingInt(
                                        reply ->
                                                getLongestKeywordLength(
                                                        reply.getKeywords()
                                                )
                                ).reversed()
                        )
                        .toList();

        cachedReplies = List.copyOf(replies);

        log.info(
                "ICT quick-reply cache refreshed. Entries: {}",
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

    if (question == null
            || question.isBlank()) {

        return Optional.empty();
    }

    String normalizedQuestion =
            normalize(question);


    for (IctQuickReply reply : cachedReplies) {

        if (reply.getKeywords() == null
                || reply.getKeywords().isBlank()) {

            continue;
        }

        String[] keywordList =
                reply.getKeywords().split(",");


        for (String keyword : keywordList) {

            String normalizedKeyword =
                    normalize(keyword);


            if (normalizedKeyword.isBlank()) {
                continue;
            }


            if (containsSafeMatch(
                    normalizedQuestion,
                    normalizedKeyword
            )) {

                log.info(
                        "ICT quick reply matched. Keyword: {}",
                        keyword.trim()
                );

                return Optional.of(
                        reply.getReplyText()
                );
            }
        }
    }

    return Optional.empty();
}


/*
 * ===================================
 * SAFE MATCH
 * ===================================
 */

private boolean containsSafeMatch(
        String question,
        String keyword
) {

    /*
     * Exact match
     */
    if (question.equals(keyword)) {
        return true;
    }


    /*
     * Phrase / word boundary match
     */
    return question.contains(
            " " + keyword + " "
    )
            || question.startsWith(
            keyword + " "
    )
            || question.endsWith(
            " " + keyword
    );
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
            .replaceAll(
                    "[?!.,।:;\"'`()\\{}]",
                    " "
            )
            .replaceAll(
                    "\\s+",
                    " "
            )
            .trim();
}


/*
 * ===================================
 * LONGEST KEYWORD
 * ===================================
 */

private int getLongestKeywordLength(
        String keywords
) {

    if (keywords == null
            || keywords.isBlank()) {

        return 0;
    }

    return Arrays.stream(
                    keywords.split(",")
            )
            .map(this::normalize)
            .mapToInt(String::length)
            .max()
            .orElse(0);
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


    if (request.getKeywords() != null
            && !request.getKeywords().isBlank()) {

        entity.setKeywords(
                request.getKeywords().trim()
        );
    }


    if (request.getReplyText() != null
            && !request.getReplyText().isBlank()) {

        entity.setReplyText(
                request.getReplyText().trim()
        );
    }


    if (request.getIsActive() != null) {

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

    if (!repository.existsById(id)) {

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


    if (request.getKeywords() == null
            || request.getKeywords().isBlank()) {

        throw new IllegalArgumentException(
                "Keywords খালি হতে পারবে না"
        );
    }


    if (request.getReplyText() == null
            || request.getReplyText().isBlank()) {

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

}
