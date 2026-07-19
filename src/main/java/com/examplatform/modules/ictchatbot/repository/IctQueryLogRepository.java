package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctQueryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IctQueryLogRepository extends JpaRepository<IctQueryLog, java.util.UUID> {

    long count();

    // প্রশ্নভিত্তিক aggregate — একটা sample answer (সবচেয়ে সাম্প্রতিক non-null উত্তর) সহ
    @Query(value = """
        SELECT
            question AS question,
            COUNT(*) AS askCount,
            SUM(CASE WHEN response_path = 'NOT_FOUND' THEN 1 ELSE 0 END) AS notFoundCount,
            SUM(CASE WHEN response_path = 'QUICK_REPLY' THEN 1 ELSE 0 END) AS quickReplyCount,
            SUM(CASE WHEN response_path = 'CACHE_HIT' THEN 1 ELSE 0 END) AS cacheHitCount,
            SUM(CASE WHEN response_path = 'GEMINI_GENERATED' THEN 1 ELSE 0 END) AS geminiGeneratedCount,
            (
                SELECT l2.answer_text
                FROM ict_query_log l2
                WHERE l2.question = l1.question
                  AND l2.answer_text IS NOT NULL
                ORDER BY l2.created_at DESC
                LIMIT 1
            ) AS sampleAnswer
        FROM ict_query_log l1
        GROUP BY question
        """, nativeQuery = true)
    List<Object[]> aggregateByQuestion();

    @Transactional
    void deleteAllInBatch();
}
