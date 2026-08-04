package com.examplatform.modules.evaluation.service;

import com.examplatform.modules.evaluation.entity.EvaluationQuestion;
import com.examplatform.modules.evaluation.entity.EvaluationResult;
import com.examplatform.modules.evaluation.entity.EvaluationRun;
import com.examplatform.modules.evaluation.enums.EvaluationResultStatus;
import com.examplatform.modules.evaluation.enums.EvaluationRunStatus;
import com.examplatform.modules.evaluation.repository.EvaluationQuestionRepository;
import com.examplatform.modules.evaluation.repository.EvaluationResultRepository;
import com.examplatform.modules.evaluation.repository.EvaluationRunRepository;
import com.examplatform.modules.ictchatbot.dto.IctAskResponse;
import com.examplatform.modules.ictchatbot.repository.IctBookChunkRepository;
import com.examplatform.modules.ictchatbot.service.EmbeddingService;
import com.examplatform.modules.ictchatbot.service.IctAskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluation-এর হৃদয়। এই ক্লাস কোনো নতুন RAG/QA লজিক তৈরি করে না —
 * শুধু existing IctAskService.ask() এবং IctBookChunkRepository কে
 * reuse করে প্রতিটা প্রশ্নের জন্য উত্তর + retrieval metadata capture করে।
 *
 * IctAskService.ask() নিজে থেকেই userId-ভিত্তিক rate-limit (১০ req/min)
 * ও answer-cache ব্যবহার করে। Production ইউজারের সাথে সংঘর্ষ/rate-limit
 * এড়াতে প্রতিটা প্রশ্নের জন্য একটা distinct synthetic userId পাঠানো হয়
 * ("eval-<runId>-<questionId>") — IctAskService-এর কোনো কোড স্পর্শ না করেই।
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationRunnerService {

    private final EvaluationRunRepository runRepository;
    private final EvaluationQuestionRepository questionRepository;
    private final EvaluationResultRepository resultRepository;

    private final IctAskService ictAskService;
    private final IctBookChunkRepository ictBookChunkRepository;
    private final EmbeddingService embeddingService;

    private static final int RETRIEVAL_TOP_K = 5;

    /**
     * পুরো run synchronous ভাবে execute করে — request thread-এ ব্লক করে।
     * বড় dataset (কয়েকশ প্রশ্ন)-এর জন্য Controller phase-এ প্রয়োজনে
     * এটাকে একটা plain background Thread থেকে কল করা যেতে পারে
     * (প্রজেক্টে @Async-এর কোনো existing precedent নেই বলে এখানে
     * নতুন async framework/pattern introduce করা হয়নি)।
     */
    @Transactional
    public void executeRun(String runId) {
        EvaluationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("EvaluationRun not found: " + runId));

        if (run.getStatus() != EvaluationRunStatus.PENDING) {
            throw new IllegalStateException("শুধু PENDING run execute করা যায়, বর্তমান স্ট্যাটাস: " + run.getStatus());
        }

        run.setStatus(EvaluationRunStatus.RUNNING);
        run.setStartedAt(LocalDateTime.now());
        runRepository.save(run);

        List<EvaluationQuestion> questions =
                questionRepository.findByDatasetIdAndIsActiveTrue(run.getDataset().getId());

        int processed = 0;
        boolean anyFailure = false;

        for (EvaluationQuestion question : questions) {
            try {
                runSingleQuestion(run, question);
            } catch (Exception e) {
                log.error("Evaluation প্রশ্ন প্রসেস করতে ব্যর্থ: runId={}, questionId={}", runId, question.getId(), e);
                saveFailedResult(run, question, e);
                anyFailure = true;
            }
            processed++;
            run.setProcessedQuestions(processed);
            runRepository.save(run);
        }

        run.setStatus(anyFailure ? EvaluationRunStatus.FAILED : EvaluationRunStatus.COMPLETED);
        run.setCompletedAt(LocalDateTime.now());
        runRepository.save(run);
    }

    private void runSingleQuestion(EvaluationRun run, EvaluationQuestion question) {
        String syntheticUserId = "eval-" + run.getId() + "-" + question.getId();

        long totalStart = System.currentTimeMillis();

        // ১) Retrieval metadata আলাদাভাবে capture (IctAskService-এর ভেতরে ঢুকে
        //    কিছু বের করা হচ্ছে না — retrieval layer independently আবার query করা হচ্ছে)
        long retrievalStart = System.currentTimeMillis();
        List<String> retrievedChunkIds = new ArrayList<>();
        List<Double> retrievedChunkDistances = new ArrayList<>();
        Double closestDistance = null;
        try {
            float[] embedding = embeddingService.generateEmbedding(question.getQuestionText());
            String embeddingStr = toVectorLiteral(embedding);

            List<Object[]> rows = ictBookChunkRepository.findSimilarChunkIdsWithDistance(
                    embeddingStr, null, RETRIEVAL_TOP_K);

            for (Object[] row : rows) {
                if (row[0] != null) {
                    retrievedChunkIds.add(row[0].toString());
                }
                if (row[1] instanceof Number number) {
                    retrievedChunkDistances.add(number.doubleValue());
                }
            }
            if (!retrievedChunkDistances.isEmpty()) {
                closestDistance = retrievedChunkDistances.get(0);
            }
        } catch (Exception e) {
            log.warn("Retrieval metadata capture ব্যর্থ হয়েছে (metric ছাড়াই এগোনো হচ্ছে): {}", e.getMessage());
        }
        int retrievalLatencyMs = (int) (System.currentTimeMillis() - retrievalStart);

        // ২) মূল answer generation — সম্পূর্ণ existing pipeline reuse
        long llmStart = System.currentTimeMillis();
        IctAskResponse response = ictAskService.ask(question.getQuestionText(), syntheticUserId);
        int llmLatencyMs = (int) (System.currentTimeMillis() - llmStart);

        int totalLatencyMs = (int) (System.currentTimeMillis() - totalStart);

        EvaluationResult result = EvaluationResult.builder()
                .run(run)
                .question(question)
                .generatedAnswer(response.getAnswer())
                .matchedWriterNames(response.getSourceWriters() != null
                        ? String.join(", ", response.getSourceWriters()) : null)
                .answerFound(response.getAnswer() != null && !response.getAnswer().isBlank())
                .fromCache(response.isFromCache())
                .retrievedChunkIds(retrievedChunkIds)
                .retrievedChunkDistances(retrievedChunkDistances)
                .closestChunkDistance(closestDistance)
                .retrievedChunkCount(retrievedChunkDistances.size())
                .candidateChunkCount(retrievedChunkDistances.size())
                .retrievalLatencyMs(retrievalLatencyMs)
                .llmLatencyMs(llmLatencyMs)
                .responseTimeMs(totalLatencyMs)
                .promptVersion(run.getPrompt().getVersion())
                .modelName(run.getProfile().getModelName())
                .status(EvaluationResultStatus.SUCCESS)
                .build();

        resultRepository.save(result);
    }

    private void saveFailedResult(EvaluationRun run, EvaluationQuestion question, Exception e) {
        EvaluationResult result = EvaluationResult.builder()
                .run(run)
                .question(question)
                .status(EvaluationResultStatus.FAILED)
                .errorMessage(e.getMessage())
                .answerFound(false)
                .fromCache(false)
                .build();
        resultRepository.save(result);
    }

    // IctBookChunkRepository.findSimilarChunks()-এর নেটিভ query যেভাবে embedding
    // string প্রত্যাশা করে ঠিক সেই ফরম্যাটে ("[0.1,0.2,...]")
    private String toVectorLiteral(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
