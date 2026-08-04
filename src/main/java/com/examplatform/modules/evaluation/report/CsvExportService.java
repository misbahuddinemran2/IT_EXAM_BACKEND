package com.examplatform.modules.evaluation.report;

import com.examplatform.modules.evaluation.entity.EvaluationResult;
import com.examplatform.modules.evaluation.entity.EvaluationRun;
import com.examplatform.modules.evaluation.repository.EvaluationResultRepository;
import com.examplatform.modules.evaluation.repository.EvaluationRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * একটা run-এর সব result কে CSV আকারে বানায় — research paper-এর জন্য
 * raw ডেটা এক্সপোর্ট। কোনো নতুন business logic নেই, শুধু existing
 * EvaluationResult ডেটা read করে ফরম্যাট করা হয়।
 */
@Service
@RequiredArgsConstructor
public class CsvExportService {

    private final EvaluationRunRepository runRepository;
    private final EvaluationResultRepository resultRepository;

    private static final String[] HEADERS = {
            "Question", "Difficulty", "Question Type",
            "Expected Answer", "Generated Answer",
            "Expected Writers", "Matched Writers",
            "Retrieved Chunk IDs", "Closest Chunk Distance",
            "Retrieved Chunk Count",
            "Retrieval Latency (ms)", "LLM Latency (ms)", "Total Latency (ms)",
            "From Cache", "Answer Found",
            "Exact Match", "Semantic Similarity", "Token F1",
            "Citation Precision", "Citation Recall", "Citation Faithfulness",
            "Status", "Error Message"
    };

    public byte[] generateCsv(String runId) {
        EvaluationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("EvaluationRun not found: " + runId));

        List<EvaluationResult> results = resultRepository.findByRunId(runId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // UTF-8 BOM — এক্সেলে বাংলা টেক্সট সঠিকভাবে রেন্ডার হওয়ার জন্য
        out.write(0xEF);
        out.write(0xBB);
        out.write(0xBF);

        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            writer.println("# Run: " + run.getId()
                    + " | Dataset: " + run.getDataset().getName()
                    + " | Model: " + run.getProfile().getModelName()
                    + " | Prompt: " + run.getPrompt().getName() + " v" + run.getPrompt().getVersion());

            writer.println(String.join(",", HEADERS));

            for (EvaluationResult r : results) {
                writer.println(toCsvRow(r));
            }
        }

        return out.toByteArray();
    }

    private String toCsvRow(EvaluationResult r) {
        String[] row = {
                escape(r.getQuestion().getQuestionText()),
                escape(r.getQuestion().getDifficulty() != null ? r.getQuestion().getDifficulty().name() : ""),
                escape(r.getQuestion().getQuestionType() != null ? r.getQuestion().getQuestionType().name() : ""),
                escape(r.getQuestion().getExpectedAnswer()),
                escape(r.getGeneratedAnswer()),
                escape(r.getQuestion().getExpectedWriterNames()),
                escape(r.getMatchedWriterNames()),
                escape(r.getRetrievedChunkIds() != null ? String.join("|", r.getRetrievedChunkIds()) : ""),
                escape(r.getClosestChunkDistance() != null ? r.getClosestChunkDistance().toString() : ""),
                escape(r.getRetrievedChunkCount() != null ? r.getRetrievedChunkCount().toString() : ""),
                escape(r.getRetrievalLatencyMs() != null ? r.getRetrievalLatencyMs().toString() : ""),
                escape(r.getLlmLatencyMs() != null ? r.getLlmLatencyMs().toString() : ""),
                escape(r.getResponseTimeMs() != null ? r.getResponseTimeMs().toString() : ""),
                escape(String.valueOf(r.isFromCache())),
                escape(String.valueOf(r.isAnswerFound())),
                escape(r.getExactMatch() != null ? r.getExactMatch().toString() : ""),
                escape(r.getSemanticSimilarityScore() != null ? r.getSemanticSimilarityScore().toString() : ""),
                escape(r.getTokenF1Score() != null ? r.getTokenF1Score().toString() : ""),
                escape(r.getCitationPrecision() != null ? r.getCitationPrecision().toString() : ""),
                escape(r.getCitationRecall() != null ? r.getCitationRecall().toString() : ""),
                escape(r.getCitationFaithfulness() != null ? r.getCitationFaithfulness().toString() : ""),
                escape(r.getStatus().name()),
                escape(r.getErrorMessage())
        };
        return String.join(",", row);
    }

    /**
     * CSV-safe escaping — কমা/quote/newline থাকলে quote দিয়ে wrap করে,
     * ভেতরের quote কে double-quote দিয়ে escape করে
     */
    private String escape(String value) {
        if (value == null) return "";
        String v = value.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");
        if (v.contains(",") || v.contains("\"") || v.contains(" ")) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }
}
