package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctQueryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IctQueryLogRepository extends JpaRepository<IctQueryLog, java.util.UUID> {

    long count();

    // প্রশ্নভিত্তিক aggregate — summarize করার সময় ব্যবহার হবে
    @Query(value = """
        SELECT
            question AS question,
            COUNT(*) AS askCount,
            SUM(CASE WHEN response_path = 'NOT_FOUND' THEN 1 ELSE 0 END) AS notFoundCount,
            SUM(CASE WHEN response_path = 'QUICK_REPLY' THEN 1 ELSE 0 END) AS quickReplyCount,
            SUM(CASE WHEN response_path = 'CACHE_HIT' THEN 1 ELSE 0 END) AS cacheHitCount,
            SUM(CASE WHEN response_path = 'GEMINI_GENERATED' THEN 1 ELSE 0 END) AS geminiGeneratedCount
        FROM ict_query_log
        GROUP BY question
        """, nativeQuery = true)
    List<Object[]> aggregateByQuestion();

    @Transactional
    void deleteAllInBatch();
}
