package com.examplatform.modules.evaluation.mapper;

import com.examplatform.modules.evaluation.dto.ResultResponse;
import com.examplatform.modules.evaluation.dto.ResultSummaryResponse;
import com.examplatform.modules.evaluation.entity.EvaluationResult;
import org.springframework.stereotype.Component;

@Component
public class EvaluationResultMapper {

    public ResultResponse toResponse(EvaluationResult r) {
        return ResultResponse.builder()
                .id(r.getId())
                .runId(r.getRun().getId())
                .questionId(r.getQuestion().getId())
                .questionText(r.getQuestion().getQuestionText())
                .expectedAnswer(r.getQuestion().getExpectedAnswer())
                .generatedAnswer(r.getGeneratedAnswer())
                .responsePath(r.getResponsePath())
                .matchedWriterNames(r.getMatchedWriterNames())
                .expectedWriterNames(r.getQuestion().getExpectedWriterNames())
                .answerFound(r.isAnswerFound())
                .fromCache(r.isFromCache())
                .retrievedChunkIds(r.getRetrievedChunkIds())
                .retrievedChunkDistances(r.getRetrievedChunkDistances())
                .closestChunkDistance(r.getClosestChunkDistance())
                .retrievedChunkCount(r.getRetrievedChunkCount())
                .candidateChunkCount(r.getCandidateChunkCount())
                .retrievalLatencyMs(r.getRetrievalLatencyMs())
                .llmLatencyMs(r.getLlmLatencyMs())
                .responseTimeMs(r.getResponseTimeMs())
                .promptVersion(r.getPromptVersion())
                .modelName(r.getModelName())
                .tokenInput(r.getTokenInput())
                .tokenOutput(r.getTokenOutput())
                .exactMatch(r.getExactMatch())
                .semanticSimilarityScore(r.getSemanticSimilarityScore())
                .tokenF1Score(r.getTokenF1Score())
                .citationCoverage(r.getCitationCoverage())
                .citationPrecision(r.getCitationPrecision())
                .citationRecall(r.getCitationRecall())
                .citationFaithfulness(r.getCitationFaithfulness())
                .status(r.getStatus().name())
                .errorMessage(r.getErrorMessage())
                .createdAt(r.getCreatedAt())
                .build();
    }

    public ResultSummaryResponse toSummaryResponse(EvaluationResult r) {
        return ResultSummaryResponse.builder()
                .id(r.getId())
                .questionText(r.getQuestion().getQuestionText())
                .status(r.getStatus().name())
                .answerFound(r.isAnswerFound())
                .semanticSimilarityScore(r.getSemanticSimilarityScore())
                .citationPrecision(r.getCitationPrecision())
                .responseTimeMs(r.getResponseTimeMs())
                .build();
    }
}
