package com.examplatform.modules.written.evaluation.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnswerMatchingService {

    // সাধারণ filler words যেগুলো matching-এ বাদ দেওয়া হবে (ঐচ্ছিক, দরকার হলে বাড়ানো যাবে)
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "are", "was", "were", "of", "in", "on", "to", "and", "or"
    );

    /**
     * দুইটা answer text-এর মধ্যে Jaccard similarity বের করে (0.0 - 1.0)
     */
    public BigDecimal calculateSimilarity(String textA, String textB) {
        if (textA == null || textB == null || textA.isBlank() || textB.isBlank()) {
            return BigDecimal.ZERO;
        }

        Set<String> tokensA = tokenize(textA);
        Set<String> tokensB = tokenize(textB);

        if (tokensA.isEmpty() || tokensB.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Set<String> intersection = new HashSet<>(tokensA);
        intersection.retainAll(tokensB);

        Set<String> union = new HashSet<>(tokensA);
        union.addAll(tokensB);

        double score = (double) intersection.size() / union.size();

        return BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * matchScore থেকে predicted mark বের করে: predictedMark = maxMark * matchScore
     */
    public BigDecimal calculatePredictedMark(BigDecimal matchScore, BigDecimal maxMark) {
        if (matchScore == null || maxMark == null) {
            return BigDecimal.ZERO;
        }
        return matchScore.multiply(maxMark).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Text normalize + tokenize করে word set বানায়
     */
    private Set<String> tokenize(String text) {
        String normalized = text.toLowerCase()
                .replaceAll("[^a-zA-Zঀ-৿0-9\\s]", " ") // punctuation বাদ, বাংলা ইউনিকোড রেঞ্জ রাখা
                .trim();

        if (normalized.isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(normalized.split("\\s+"))
                .filter(word -> !word.isBlank())
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }
}