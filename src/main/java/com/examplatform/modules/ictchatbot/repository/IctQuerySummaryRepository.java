package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctQuerySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface IctQuerySummaryRepository extends JpaRepository<IctQuerySummary, UUID> {

    // সবচেয়ে বেশি Gemini call হওয়া প্রশ্ন — quick-reply তে যোগ করার candidate
    @Query("""
        SELECT s FROM IctQuerySummary s
        ORDER BY s.geminiGeneratedCount DESC
        """)
    List<IctQuerySummary> findTopByGeminiCallDesc();

    // সবচেয়ে বেশি NOT_FOUND হওয়া প্রশ্ন — content gap / off-topic junk চেনার জন্য
    @Query("""
        SELECT s FROM IctQuerySummary s
        ORDER BY s.notFoundCount DESC
        """)
    List<IctQuerySummary> findTopByNotFoundDesc();

    List<IctQuerySummary> findAllByOrderByCreatedAtDesc();
}
