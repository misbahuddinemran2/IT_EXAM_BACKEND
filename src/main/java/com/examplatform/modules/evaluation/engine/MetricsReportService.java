package com.examplatform.modules.evaluation.engine;

import com.examplatform.modules.evaluation.dto.RunComparisonResponse;
import com.examplatform.modules.evaluation.dto.RunMetricSummaryResponse;
import com.examplatform.modules.evaluation.entity.EvaluationRun;
import com.examplatform.modules.evaluation.enums.EvaluationResultStatus;
import com.examplatform.modules.evaluation.repository.EvaluationResultRepository;
import com.examplatform.modules.evaluation.repository.EvaluationRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricsReportService {

    private final EvaluationRunRepository runRepository;
    private final EvaluationResultRepository resultRepository;

    public RunMetricSummaryResponse buildSummary(String runId) {
        EvaluationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("EvaluationRun not found: " + runId));

        List<Object[]> aggRows = resultRepository.findMetricAggregatesByRunId(runId);
        Object[] agg = (aggRows != null && !aggRows.isEmpty()) ? aggRows.get(0) : new Object[11];

        long totalQuestions = run.getTotalQuestions() != null ? run.getTotalQuestions() : 0;
        long exactMatchCount = agg[9] != null ? ((Number) agg[9]).longValue() : 0;
        long successCount = agg[10] != null ? ((Number) agg[10]).longValue() : 0;

        long cacheHitCount = resultRepository.countByRunIdAndFromCacheTrue(runId);
        long answerFoundCount = resultRepository.countByRunIdAndAnswerFoundTrue(runId);
        long failedCount = resultRepository.countByRunIdAndStatus(runId, EvaluationResultStatus.FAILED);

        return RunMetricSummaryResponse.builder()
                .runId(run.getId())
                .modelName(run.getProfile().getModelName())
                .totalQuestions(totalQuestions)
                .exactMatchCount(exactMatchCount)
                .exactMatchRate(rate(exactMatchCount, successCount))
                .avgSemanticSimilarity(toBigDecimal(agg[0]))
                .avgTokenF1(toBigDecimal(agg[1]))
                .avgCitationPrecision(toBigDecimal(agg[2]))
                .avgCitationRecall(toBigDecimal(agg[3]))
                .avgRetrievalLatencyMs(toBigDecimal(agg[4]))
                .avgLlmLatencyMs(toBigDecimal(agg[5]))
                .avgResponseTimeMs(toBigDecimal(agg[6]))
                .avgTokenInput(toBigDecimal(agg[7]))
                .avgTokenOutput(toBigDecimal(agg[8]))
                .cacheHitRate(rate(cacheHitCount, totalQuestions))
                .successRate(rate(successCount, totalQuestions))
                .build();
    }

    public RunComparisonResponse compareRuns(List<String> runIds) {
        List<RunMetricSummaryResponse> summaries = runIds.stream()
                .map(this::buildSummary)
                .toList();
        return RunComparisonResponse.builder().runs(summaries).build();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(((Number) value).doubleValue()).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(long part, long total) {
        if (total == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf((double) part / total).setScale(4, RoundingMode.HALF_UP);
    }
}
