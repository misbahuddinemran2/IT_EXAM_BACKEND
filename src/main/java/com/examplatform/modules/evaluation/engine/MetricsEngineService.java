package com.examplatform.modules.evaluation.engine;

import com.examplatform.modules.evaluation.entity.EvaluationQuestion;
import com.examplatform.modules.evaluation.entity.EvaluationResult;
import com.examplatform.modules.evaluation.enums.EvaluationResultStatus;
import com.examplatform.modules.evaluation.repository.EvaluationResultRepository;
import com.examplatform.modules.ictchatbot.entity.IctBookChunk;
import com.examplatform.modules.ictchatbot.repository.IctBookChunkRepository;
import com.examplatform.modules.written.evaluation.service.AnswerMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * প্রতিটা EvaluationResult-এর raw capture ডেটা থেকে মেট্রিক গণনা করে fill করে।
 * কোনো নতুন similarity লজিক তৈরি করা হয়নি — semantic similarity/faithfulness-এর
 * জন্য existing AnswerMatchingService (written/evaluation মডিউল) সরাসরি reuse
 * করা হয়েছে। Token-F1 এবং exact-match এর জন্য একটা ছোট, স্বতন্ত্র token-overlap
 * ইউটিলিটি লেখা হয়েছে (AnswerMatchingService-এর TF-IDF লজিকের সাথে সম্পর্কহীন,
 * তাই সেটাকে duplicate করছে না)।
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsEngineService {

    private final EvaluationResultRepository resultRepository;
    private final IctBookChunkRepository ictBookChunkRepository;
    private final AnswerMatchingService answerMatchingService;

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "are", "was", "were", "of", "in", "on", "to", "and", "or",
            "এবং", "অথবা", "কিন্তু", "যে", "যা", "এই", "সেই", "এটি", "এটা", "তার",
            "হয়", "হয়েছে", "হবে", "করে", "একটি", "একটা", "থেকে", "সাথে", "জন্য", "মধ্যে"
    );

    @Transactional
    public void computeMetricsForRun(String runId) {
        List<EvaluationResult> results = resultRepository.findByRunIdAndStatus(runId, EvaluationResultStatus.SUCCESS);
        for (EvaluationResult result : results) {
            try {
                computeMetricsForResult(result);
                resultRepository.save(result);
            } catch (Exception e) {
                log.warn("Metric গণনা ব্যর্থ হয়েছে resultId={}: {}", result.getId(), e.getMessage());
            }
        }
    }

    public void computeMetricsForResult(EvaluationResult result) {
        EvaluationQuestion question = result.getQuestion();
        String expectedAnswer = question.getExpectedAnswer();
        String generatedAnswer = result.getGeneratedAnswer();

        // ===== Answer-quality metric =====
        result.setExactMatch(isExactMatch(expectedAnswer, generatedAnswer));
        result.setSemanticSimilarityScore(answerMatchingService.calculateSimilarity(expectedAnswer, generatedAnswer));
        result.setTokenF1Score(calculateTokenF1(expectedAnswer, generatedAnswer));

        // ===== Citation metric (expected writer names vs matched writer names) =====
        Set<String> expectedWriters = splitNames(question.getExpectedWriterNames());
        Set<String> matchedWriters = splitNames(result.getMatchedWriterNames());

        result.setCitationCoverage(matchedWriters.isEmpty() ? BigDecimal.ZERO : BigDecimal.ONE);
        result.setCitationPrecision(calculatePrecision(expectedWriters, matchedWriters));
        result.setCitationRecall(calculateRecall(expectedWriters, matchedWriters));
        result.setCitationFaithfulness(calculateFaithfulness(generatedAnswer, result.getRetrievedChunkIds()));
    }

    // ===== Exact match (normalized string equality) =====
    private Boolean isExactMatch(String expected, String generated) {
        if (expected == null || generated == null) return false;
        return normalize(expected).equals(normalize(generated));
    }

    private String normalize(String text) {
        return text.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    // ===== Token-level F1 (precision/recall of overlapping tokens) =====
    private BigDecimal calculateTokenF1(String expected, String generated) {
        if (expected == null || generated == null || expected.isBlank() || generated.isBlank()) {
            return BigDecimal.ZERO;
        }

        List<String> expectedTokens = tokenize(expected);
        List<String> generatedTokens = tokenize(generated);

        if (expectedTokens.isEmpty() || generatedTokens.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Set<String> expectedSet = new HashSet<>(expectedTokens);
        Set<String> generatedSet = new HashSet<>(generatedTokens);

        Set<String> overlap = new HashSet<>(expectedSet);
        overlap.retainAll(generatedSet);

        if (overlap.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double precision = (double) overlap.size() / generatedSet.size();
        double recall = (double) overlap.size() / expectedSet.size();
        double f1 = 2 * precision * recall / (precision + recall);

        return BigDecimal.valueOf(f1).setScale(4, RoundingMode.HALF_UP);
    }

    private List<String> tokenize(String text) {
        String normalized = text.toLowerCase()
                .replaceAll("[^a-zA-Zঀ-৿0-9\\s]", " ")
                .trim();
        if (normalized.isEmpty()) return List.of();

        List<String> tokens = new ArrayList<>();
        for (String word : normalized.split("\\s+")) {
            if (!word.isBlank() && !STOP_WORDS.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    // ===== Citation precision/recall (writer-name set overlap) =====
    private Set<String> splitNames(String csv) {
        if (csv == null || csv.isBlank()) return Set.of();
        Set<String> names = new HashSet<>();
        for (String name : csv.split(",")) {
            String trimmed = name.trim().toLowerCase();
            if (!trimmed.isEmpty()) names.add(trimmed);
        }
        return names;
    }

    private BigDecimal calculatePrecision(Set<String> expected, Set<String> matched) {
        if (matched.isEmpty()) return BigDecimal.ZERO;
        Set<String> overlap = new HashSet<>(expected);
        overlap.retainAll(matched);
        return BigDecimal.valueOf((double) overlap.size() / matched.size()).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRecall(Set<String> expected, Set<String> matched) {
        if (expected.isEmpty()) return BigDecimal.ZERO;
        Set<String> overlap = new HashSet<>(expected);
        overlap.retainAll(matched);
        return BigDecimal.valueOf((double) overlap.size() / expected.size()).setScale(4, RoundingMode.HALF_UP);
    }

    // ===== Faithfulness — generated answer কতটা retrieved chunk content-এর
    // সাথে সামঞ্জস্যপূর্ণ (hallucination-এর প্রাথমিক প্রক্সি মেট্রিক) =====
    private BigDecimal calculateFaithfulness(String generatedAnswer, List<String> retrievedChunkIds) {
        if (generatedAnswer == null || generatedAnswer.isBlank()
                || retrievedChunkIds == null || retrievedChunkIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        StringBuilder combinedContext = new StringBuilder();
        for (String chunkId : retrievedChunkIds) {
            ictBookChunkRepository.findById(chunkId).map(IctBookChunk::getContent)
                    .ifPresent(content -> combinedContext.append(content).append(" "));
        }

        if (combinedContext.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return answerMatchingService.calculateSimilarity(combinedContext.toString(), generatedAnswer);
    }
}
