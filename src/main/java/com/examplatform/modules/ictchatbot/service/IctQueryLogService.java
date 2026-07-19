package com.examplatform.modules.ictchatbot.service;

import com.examplatform.modules.ictchatbot.entity.IctQueryLog;
import com.examplatform.modules.ictchatbot.entity.IctQuerySummary;
import com.examplatform.modules.ictchatbot.repository.IctQueryLogRepository;
import com.examplatform.modules.ictchatbot.repository.IctQuerySummaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IctQueryLogService {

    private final IctQueryLogRepository queryLogRepository;
    private final IctQuerySummaryRepository querySummaryRepository;

    public void log(
            String userId,
            String question,
            String responsePath,
            boolean answerFound,
            String matchedWriterNames,
            Double closestChunkDistance,
            long responseTimeMs,
            String answerText
    ) {

        IctQueryLog entry = IctQueryLog.builder()
                .userId(userId != null ? userId : "anonymous")
                .question(question)
                .responsePath(responsePath)
                .answerFound(answerFound)
                .matchedWriterNames(matchedWriterNames)
                .closestChunkDistance(closestChunkDistance)
                .responseTimeMs((int) responseTimeMs)
                .answerText(answerText)
                .build();

        queryLogRepository.save(entry);
    }

    public long getRawLogCount() {
        return queryLogRepository.count();
    }

    @Transactional
    public int summarizeOnly() {

        List<Object[]> rows = queryLogRepository.aggregateByQuestion();

        if (rows.isEmpty()) {
            return 0;
        }

        LocalDateTime periodEnd = LocalDateTime.now();

        int savedCount = 0;

        for (Object[] row : rows) {

            String question = (String) row[0];
            long askCount = ((Number) row[1]).longValue();
            long notFoundCount = ((Number) row[2]).longValue();
            long quickReplyCount = ((Number) row[3]).longValue();
            long cacheHitCount = ((Number) row[4]).longValue();
            long geminiGeneratedCount = ((Number) row[5]).longValue();
            String sampleAnswer = row[6] != null ? (String) row[6] : null;

            IctQuerySummary summary = IctQuerySummary.builder()
                    .periodStart(periodEnd)
                    .periodEnd(periodEnd)
                    .question(question)
                    .askCount((int) askCount)
                    .notFoundCount((int) notFoundCount)
                    .quickReplyCount((int) quickReplyCount)
                    .cacheHitCount((int) cacheHitCount)
                    .geminiGeneratedCount((int) geminiGeneratedCount)
                    .sampleAnswer(sampleAnswer)
                    .build();

            querySummaryRepository.save(summary);
            savedCount++;
        }

        log.info("Query log summarized. {} summary rows created.", savedCount);

        return savedCount;
    }

    @Transactional
    public long cleanupRawLog() {

        long countBefore = queryLogRepository.count();

        queryLogRepository.deleteAllInBatch();

        log.info("Raw query log cleaned up. {} rows deleted.", countBefore);

        return countBefore;
    }

    @Transactional
    public int summarizeAndCleanup() {

        int summaryCount = summarizeOnly();

        cleanupRawLog();

        return summaryCount;
    }

    public List<IctQuerySummary> getTopByGeminiCalls() {
        return querySummaryRepository.findTopByGeminiCallDesc();
    }

    public List<IctQuerySummary> getTopByNotFound() {
        return querySummaryRepository.findTopByNotFoundDesc();
    }

    public List<IctQuerySummary> getAllSummaries() {
        return querySummaryRepository.findAllByOrderByCreatedAtDesc();
    }
}
