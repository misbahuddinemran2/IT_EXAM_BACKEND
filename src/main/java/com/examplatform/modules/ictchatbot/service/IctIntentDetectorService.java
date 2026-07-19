package com.examplatform.modules.ictchatbot.service;

import com.examplatform.modules.ictchatbot.repository.IctIntentKeywordRepository;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

/*
 * ===================================
 * ICT INTENT DETECTOR SERVICE
 *
 * আগে static class ছিল, এখন DB-backed —
 * admin panel থেকে keyword add/edit/delete
 * করা যায়, keyword-list কোড deploy ছাড়াই বদলায়।
 *
 * Intent enum গুলো এখনো fixed কোডেই থাকে
 * (নতুন intent category যোগ করতে হলে migration
 * + enum দুটোতেই পরিবর্তন লাগবে), কিন্তু প্রতিটা
 * intent এর keyword list dynamic।
 * ===================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IctIntentDetectorService {

    private final IctIntentKeywordRepository repository;


    public enum Intent {
        FEATURES,
        ADVANTAGE,
        DISADVANTAGE,
        ADVANTAGE_DISADVANTAGE_COMBINED,
        APPLICATION,
        EXAMPLE,
        COMPARISON,
        PROCESS,
        IMPORTANCE,
        CLASSIFICATION,
        FORMULA_CALCULATION,
        STRUCTURE,
        FULL_FORM,
        SYNTAX_CODE,
        CONDITION_REQUIREMENT,
        RULE_LAW,
        DEFINITION,   // সবচেয়ে generic, priority order এ সবার শেষে
        UNKNOWN
    }


    /*
     * ===================================
     * PRIORITY ORDER
     *
     * detect() এ এই order অনুযায়ী চেক হবে।
     * বেশি specific intent আগে, DEFINITION সবার শেষে
     * (কারণ "কী/কি" প্রায় সব প্রশ্নেই থাকতে পারে)।
     * ===================================
     */
    private static final List<Intent> PRIORITY_ORDER = List.of(
            Intent.ADVANTAGE_DISADVANTAGE_COMBINED,
            Intent.FULL_FORM,
            Intent.SYNTAX_CODE,
            Intent.FORMULA_CALCULATION,
            Intent.CLASSIFICATION,
            Intent.STRUCTURE,
            Intent.CONDITION_REQUIREMENT,
            Intent.RULE_LAW,
            Intent.FEATURES,
            Intent.ADVANTAGE,
            Intent.DISADVANTAGE,
            Intent.APPLICATION,
            Intent.EXAMPLE,
            Intent.COMPARISON,
            Intent.PROCESS,
            Intent.IMPORTANCE,
            Intent.DEFINITION
    );


    /*
     * ===================================
     * IN-MEMORY CACHE
     * ===================================
     */

    private volatile Map<Intent, List<String>> cachedKeywords = Map.of();


    @PostConstruct
    public void init() {
        refreshCache();
    }


    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void refreshCache() {

        try {

            Map<Intent, List<String>> map = new EnumMap<>(Intent.class);

            for (var entry : repository.findByIsActiveTrue()) {

                Intent intent;

                try {
                    intent = Intent.valueOf(entry.getIntent());
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown intent in DB, skipping: {}", entry.getIntent());
                    continue;
                }

                if (entry.getKeyword() == null || entry.getKeyword().isBlank()) {
                    continue;
                }

                map.computeIfAbsent(intent, k -> new ArrayList<>())
                        .add(entry.getKeyword().trim().toLowerCase(Locale.ROOT));
            }

            // প্রতিটা intent এর keyword length অনুযায়ী sort (longer আগে)
            map.replaceAll((intent, keywords) ->
                    keywords.stream()
                            .sorted(Comparator.comparingInt(String::length).reversed())
                            .toList()
            );

            cachedKeywords = Map.copyOf(map);

            log.info("ICT intent-keyword cache refreshed. Intents loaded: {}", cachedKeywords.size());

        } catch (Exception e) {
            log.error("ICT intent-keyword cache refresh failed", e);
        }
    }


    /*
     * ===================================
     * DETECT
     * ===================================
     */

    public Intent detect(String normalizedQuestion) {

        if (normalizedQuestion == null || normalizedQuestion.isBlank()) {
            return Intent.UNKNOWN;
        }

        for (Intent intent : PRIORITY_ORDER) {

            List<String> keywords = cachedKeywords.get(intent);

            if (keywords == null) {
                continue;
            }

            for (String keyword : keywords) {

                if (normalizedQuestion.contains(keyword)) {
                    return intent;
                }
            }
        }

        return Intent.UNKNOWN;
    }


    /*
     * ===================================
     * EXTRACT TOPIC
     * ===================================
     */

    public String extractTopic(String normalizedQuestion, Intent intent) {

        if (normalizedQuestion == null || normalizedQuestion.isBlank()) {
            return "";
        }

        String result = normalizedQuestion;

        if (intent != Intent.UNKNOWN) {

            List<String> keywords = cachedKeywords.get(intent);

            if (keywords != null) {
                for (String keyword : keywords) {
                    result = result.replace(keyword, " ");
                }
            }
        }

        result = result
                .replaceAll("\\b(এর|কি|কী|দাও|বল|বলো)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return result;
    }
}
