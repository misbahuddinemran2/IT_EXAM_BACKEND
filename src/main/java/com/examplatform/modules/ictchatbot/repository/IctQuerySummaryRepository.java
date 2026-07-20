package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctQuerySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IctQuerySummaryRepository extends JpaRepository<IctQuerySummary, UUID> {

    Optional<IctQuerySummary> findByQuestion(String question);

    @Query("""
        SELECT s FROM IctQuerySummary s
        ORDER BY s.geminiGeneratedCount DESC
        """)
    List<IctQuerySummary> findTopByGeminiCallDesc();

    @Query("""
        SELECT s FROM IctQuerySummary s
        ORDER BY s.notFoundCount DESC
        """)
    List<IctQuerySummary> findTopByNotFoundDesc();

    // সবচেয়ে কম জিজ্ঞাসা হওয়া প্রশ্ন আগে — bulk-delete সহজ করার জন্য
    @Query("""
        SELECT s FROM IctQuerySummary s
        ORDER BY s.askCount ASC
        """)
    List<IctQuerySummary> findTopByAskCountAsc();

    List<IctQuerySummary> findAllByOrderByCreatedAtDesc();
}
