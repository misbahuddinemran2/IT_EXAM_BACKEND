package com.examplatform.modules.ictchatbot.service;

import com.examplatform.modules.ictchatbot.dto.IctQuickReplyRequest;
import com.examplatform.modules.ictchatbot.dto.IctQuickReplyResponse;
import com.examplatform.modules.ictchatbot.entity.IctQuickReply;
import com.examplatform.modules.ictchatbot.repository.IctQuickReplyRepository;
import com.examplatform.modules.ictchatbot.repository.IctSynonymRepository;

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
private final IctIntentDetectorService intentDetectorService;
private final IctSynonymRepository synonymRepository;


/*
 * ===================================
 * QUICK REPLY MATCH RESULT
 *
 * matchType: "EXACT" / "SMART" / "CONDITIONAL" / "NONE"
 * answer: শুধু EXACT/SMART এ থাকবে (CONDITIONAL/NONE এ null)
 * score: শুধু SMART/CONDITIONAL এ থাকবে
 * matchedKeyword: যে keyword দিয়ে match/near-match হয়েছে
 * ===================================
 */

public record QuickReplyMatchResult(
        String answer,
        String matchType,
        Double score,
        String matchedKeyword
) {

    public boolean isMatched() {
        return answer != null;
    }

    public static QuickReplyMatchResult none() {
        return new QuickReplyMatchResult(null, "NONE", null, null);
    }
}


/*
 * ===================================
 * IN-MEMORY CACHE
 *
 * প্রতিটা entry এখানে আগে থেকেই
 * split + normalize করা থাকে,
 * যাতে match() কলে বারবার
 * এই কাজ করতে না হয়।
 * ===================================
 */

private volatile List<QuickReplyPattern> cachedReplies =
        List.of();


/*
 * ===================================
 * SYNONYM CACHE
 *
 * word (normalize করা) → canonical word
 * ===================================
 */

private volatile Map<String, String> cachedSynonyms =
        Map.of();


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

        Map<String, String> synonymMap = new HashMap<>();

        for (var entry : synonymRepository.findByIsActiveTrue()) {

            String word = normalizeSynonymWord(entry.getWord());
            String canonical = normalizeSynonymWord(entry.getCanonicalWord());

            if (!word.isBlank() && !canonical.isBlank()) {
                synonymMap.put(word, canonical);
            }
        }

        cachedSynonyms = Map.copyOf(synonymMap);

        log.info("ICT synonym cache refreshed. Pairs: {}", cachedSynonyms.size());

    } catch (Exception e) {
        log.error("ICT synonym cache refresh failed", e);
    }

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

                                        IctIntentDetectorService.Intent intent =
                                                intentDetectorService.detect(keyword);

                                        String topic =
                                                intentDetectorService.extractTopic(
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
 * QUICK REPLY MATCH (public entry point)
 *
 * ধাপ ১: original প্রশ্ন দিয়ে EXACT/SMART চেষ্টা
 * ধাপ ২: না পেলে synonym expansion করে আবার চেষ্টা
 * ===================================
 */

public QuickReplyMatchResult match(
        String question
) {


    if (
            question == null
                    || question.isBlank()
    ) {

        return QuickReplyMatchResult.none();
    }


    String normalizedQuestion =
            normalize(question);


    // ধাপ ১: original প্রশ্ন দিয়ে প্রথম পাস
    QuickReplyMatchResult firstPass = tryMatch(normalizedQuestion);

    if ("EXACT".equals(firstPass.matchType()) || "SMART".equals(firstPass.matchType())) {
        return firstPass;
    }


    // ধাপ ২: synonym expansion করে দ্বিতীয় পাস
    
// ধাপ ২: synonym expansion করে দ্বিতীয় পাস
String expandedQuestion = applySynonyms(normalizedQuestion);

if (!expandedQuestion.equals(normalizedQuestion)) {

    log.info(
            "ICT synonym applied. Original: {}, Expanded: {}",
            normalizedQuestion, expandedQuestion
    );

    QuickReplyMatchResult secondPass = tryMatch(expandedQuestion);

    if ("EXACT".equals(secondPass.matchType()) || "SMART".equals(secondPass.matchType())) {

        log.info(
                "ICT quick reply matched via synonym expansion. Original: {}, Expanded: {}",
                normalizedQuestion, expandedQuestion
        );

        return secondPass;
    }

    log.info(
            "ICT synonym expanded but still no match. Expanded: {}, ResultType: {}, Score: {}",
            expandedQuestion, secondPass.matchType(), secondPass.score()
    );

    // synonym pass এ প্রথম পাসের চেয়ে ভালো CONDITIONAL score পেলে সেটাই নেওয়া হবে
    if ("CONDITIONAL".equals(secondPass.matchType())
            && (firstPass.score() == null
                || (secondPass.score() != null && secondPass.score() > firstPass.score()))) {

        return secondPass;
    }
} else {

    log.info(
            "ICT synonym not applied (no matching word found). Question: {}",
            normalizedQuestion
    );
}

    // কোনো পাসেই ভালো কিছু না পেলে প্রথম পাসের ফলাফলই ফেরত (NONE বা CONDITIONAL)
    return firstPass;
}


/*
 * ===================================
 * TRY MATCH
 *
 * একটা normalize করা প্রশ্ন দিয়ে
 * EXACT match চেষ্টা, না পেলে SMART match
 * (আগের match() মেথডের মূল লজিক, অপরিবর্তিত)
 * ===================================
 */

private QuickReplyMatchResult tryMatch(
        String normalizedQuestion
) {


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


            return new QuickReplyMatchResult(
                    pattern.replyText(),
                    "EXACT",
                    null,
                    pattern.keyword()
            );
        }
    }


    return smartMatch(normalizedQuestion);
}


/*
 * ===================================
 * SMART SIMILARITY MATCH
 *
 * Text Similarity + Intent Match + Topic Match
 * একসাথে যাচাই করে confidence score বের করে।
 *
 * Score >= 95  → সরাসরি Quick Reply Answer (SMART)
 * Score 90-95  → CONDITIONAL, answer না, শুধু matchType/score log হয়
 * Score < 90   → NONE, existing embedding flow এ যাবে
 * ===================================
 */

private QuickReplyMatchResult smartMatch(
        String normalizedQuestion
) {


    if (
            normalizedQuestion == null
                    || normalizedQuestion.isBlank()
                    || cachedReplies.isEmpty()
    ) {

        return QuickReplyMatchResult.none();
    }


    IctIntentDetectorService.Intent questionIntent =
            intentDetectorService.detect(normalizedQuestion);


    String questionTopic =
            intentDetectorService.extractTopic(
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
                questionIntent != IctIntentDetectorService.Intent.UNKNOWN
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
     * HIGH CONFIDENCE → সরাসরি answer (SMART)
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


        return new QuickReplyMatchResult(
                bestMatch.replyText(),
                "SMART",
                bestScore,
                bestMatch.keyword()
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


        return new QuickReplyMatchResult(
                null,
                "CONDITIONAL",
                bestScore,
                bestMatch.keyword()
        );
    }


    return QuickReplyMatchResult.none();
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
 * NORMALIZE SYNONYM WORD
 *
 * synonym টেবিলের word/canonical_word
 * ছোট normalize (trim + lowercase),
 * মূল normalize() থেকে হালকা রাখা হয়েছে
 * ===================================
 */

private String normalizeSynonymWord(String text) {

    if (text == null) {
        return "";
    }

    return text.trim().toLowerCase(Locale.ROOT);
}


/*
 * ===================================
 * APPLY SYNONYMS
 *
 * প্রশ্নের প্রতিটা শব্দ/phrase synonym টেবিলে
 * থাকলে canonical word দিয়ে replace করে।
 * Longer phrase আগে try হয় (multi-word synonym safety)।
 * ===================================
 */

private String applySynonyms(String normalizedQuestion) {

    if (normalizedQuestion == null
            || normalizedQuestion.isBlank()
            || cachedSynonyms.isEmpty()) {

        return normalizedQuestion;
    }

    String result = normalizedQuestion;

    List<String> sortedKeys = cachedSynonyms.keySet().stream()
            .sorted(Comparator.comparingInt(String::length).reversed())
            .toList();

    for (String word : sortedKeys) {

        String canonical = cachedSynonyms.get(word);

        if (word.contains(" ")) {

            if (result.contains(word)) {
                result = result.replace(word, canonical);
            }

        } else {

            result = result.replaceAll(
                    "\\b" + java.util.regex.Pattern.quote(word) + "\\b",
                    canonical
            );
        }
    }

    return result.replaceAll("\\s+", " ").trim();
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
        IctIntentDetectorService.Intent intent
) {
}

}
