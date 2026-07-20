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
import java.util.Optional;
import java.util.UUID;

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
            String answerText,
            String quickReplyMatchType,
            Double quickReplyMatchScore,
            String quickReplyMatchedKeyword
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
                .quickReplyMatchType(quickReplyMatchType)
                .quickReplyMatchScore(quickReplyMatchScore)
                .quickReplyMatchedKeyword(quickReplyMatchedKeyword)
                .build();

        queryLogRepository.save(entry);
    }

    public long getRawLogCount() {
        return queryLogRepository.count();
    }

    /*
     * Summarize এখন UPSERT করে — একই প্রশ্ন আগে থেকে summary টেবিলে থাকলে
     * সেই row এর count গুলো নতুন raw log এর ভ্যালু দিয়ে যোগ (accumulate) হবে,
     * নতুন row তৈরি হবে না। এভাবে বারবার summarize করলেও ডুপ্লিকেট জমবে না।
     */
    @Transactional
    public int summarizeOnly() {

        List<Object[]> rows = queryLogRepository.aggregateByQuestion();

        if (rows.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();

        int processedCount = 0;

        for (Object[] row : rows) {

            String question = (String) row[0];
            long askCount = ((Number) row[1]).longValue();
            long notFoundCount = ((Number) row[2]).longValue();
            long quickReplyCount = ((Number) row[3]).longValue();
            long cacheHitCount = ((Number) row[4]).longValue();
            long geminiGeneratedCount = ((Number) row[5]).longValue();
            String sampleAnswer = row[6] != null ? (String) row[6] : null;

            Optional<IctQuerySummary> existing =
                    querySummaryRepository.findByQuestion(question);

            if (existing.isPresent()) {

                IctQuerySummary summary = existing.get();

                summary.setAskCount(summary.getAskCount() + (int) askCount);
                summary.setNotFoundCount(summary.getNotFoundCount() + (int) notFoundCount);
                summary.setQuickReplyCount(summary.getQuickReplyCount() + (int) quickReplyCount);
                summary.setCacheHitCount(summary.getCacheHitCount() + (int) cacheHitCount);
                summary.setGeminiGeneratedCount(summary.getGeminiGeneratedCount() + (int) geminiGeneratedCount);
                summary.setPeriodEnd(now);

                if (sampleAnswer != null) {
                    summary.setSampleAnswer(sampleAnswer);
                }

                querySummaryRepository.save(summary);

            } else {

                IctQuerySummary summary = IctQuerySummary.builder()
                        .periodStart(now)
                        .periodEnd(now)
                        .question(question)
                        .askCount((int) askCount)
                        .notFoundCount((int) notFoundCount)
                        .quickReplyCount((int) quickReplyCount)
                        .cacheHitCount((int) cacheHitCount)
                        .geminiGeneratedCount((int) geminiGeneratedCount)
                        .sampleAnswer(sampleAnswer)
                        .build();

                querySummaryRepository.save(summary);
            }

            processedCount++;
        }

        log.info("Query log summarized (upsert). {} questions processed.", processedCount);

        return processedCount;
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
    
public List<IctQuerySummary> getLeastAsked() {
        return querySummaryRepository.findTopByAskCountAsc();
    }
    // CONDITIONAL zone raw log entries (matchType tuning এর জন্য)
    public List<IctQueryLog> getConditionalZoneLogs() {
        return queryLogRepository.findByQuickReplyMatchTypeOrderByCreatedAtDesc("CONDITIONAL");
    }

    /*
     * ===================================
     * SUMMARY DELETE (individual + bulk)
     * ===================================
     */

    @Transactional
    public void deleteSummary(UUID id) {
        querySummaryRepository.deleteById(id);
        log.info("Summary entry deleted. id={}", id);
    }

    @Transactional
    public int deleteSummaries(List<UUID> ids) {

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        List<IctQuerySummary> toDelete = querySummaryRepository.findAllById(ids);

        querySummaryRepository.deleteAll(toDelete);

        log.info("Bulk summary delete. {} entries deleted.", toDelete.size());

        return toDelete.size();
    }
}
