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
                                    .map(keyword -> {

                                        IctIntentDetector.Intent intent =
                                                IctIntentDetector.detect(keyword);

                                        String topic =
                                                IctIntentDetector.extractTopic(
                                                        keyword, intent
                                                );

                                        return new QuickReplyPattern(
                                                keyword,
                                                reply.getReplyText(),
                                                topic,
                                                intent
                                        );
                                    });
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


    /*
     * ধাপ ১: Existing exact/phrase/word match
     * (অপরিবর্তিত, আগের মতোই)
     */

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
                    "ICT quick reply matched (exact). Keyword: {}",
                    pattern.keyword()
            );


            return Optional.of(
                    pattern.replyText()
            );
        }
    }


    /*
     * ধাপ ২: Exact match না পেলে,
     * Smart Similarity Matcher fallback
     */

    return smartMatch(normalizedQuestion);
}


/*
 * ===================================
 * SMART SIMILARITY MATCH
 *
 * Text Similarity + Intent Match + Topic Match
 * একসাথে যাচাই করে confidence score বের করে।
 *
 * Score >= 95  → সরাসরি Quick Reply Answer
 * Score 90-95  → শুধু log (conservative, প্রথম
 *                version এ answer দেওয়া হবে না)
 * Score < 90   → No match, existing embedding
 *                flow এ যাবে
 * ===================================
 */

private Optional<String> smartMatch(
        String normalizedQuestion
) {


    if (
            normalizedQuestion == null
                    || normalizedQuestion.isBlank()
                    || cachedReplies.isEmpty()
    ) {

        return Optional.empty();
    }


    IctIntentDetector.Intent questionIntent =
            IctIntentDetector.detect(normalizedQuestion);


    String questionTopic =
            IctIntentDetector.extractTopic(
                    normalizedQuestion,
                    questionIntent
            );


    QuickReplyPattern bestMatch = null;
    double bestScore = 0.0;


    for (
            QuickReplyPattern pattern :
            cachedReplies
    ) {


        double textSim =
                QuickReplySimilarityUtil.combinedSimilarity(
                        normalizedQuestion,
                        pattern.keyword()
                );


        boolean intentMatch =
                questionIntent != IctIntentDetector.Intent.UNKNOWN
                        && questionIntent == pattern.intent();


        double topicSim =
                QuickReplySimilarityUtil.combinedSimilarity(
                        questionTopic,
                        pattern.topic()
                );

        boolean topicMatch = topicSim >= 0.7;


        double score =
                (textSim * 35)
                        + (intentMatch ? 40 : 0)
                        + (topicMatch ? 25 : 0);


        if (score > bestScore) {

            bestScore = score;
            bestMatch = pattern;
        }
    }


    /*
     * HIGH CONFIDENCE → সরাসরি answer
     */

    if (
            bestMatch != null
                    && bestScore >= 95
    ) {


        log.info(
                "ICT quick reply matched (smart). Score: {}, Keyword: {}, Question: {}",
                bestScore,
                bestMatch.keyword(),
                normalizedQuestion
        );


        return Optional.of(
                bestMatch.replyText()
        );
    }


    /*
     * CONDITIONAL ZONE → শুধু log, answer না
     *
     * এই zone এর data দেখে ভবিষ্যতে
     * threshold/weight tune করা হবে।
     */

    if (
            bestMatch != null
                    && bestScore >= 90
    ) {


        log.info(
                "ICT quick reply CONDITIONAL zone (not served). Score: {}, Keyword: {}, Question: {}",
                bestScore,
                bestMatch.keyword(),
                normalizedQuestion
        );
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
 *
 * topic ও intent এখন cache-refresh এর সময়ই
 * precompute করে রাখা হয়, প্রতি request এ
 * বারবার হিসাব না করার জন্য (performance)
 * ===================================
 */

private record QuickReplyPattern(
        String keyword,
        String replyText,
        String topic,
        IctIntentDetector.Intent intent
) {
}

}
