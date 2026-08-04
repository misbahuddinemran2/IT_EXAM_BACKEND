package com.examplatform.modules.evaluation.repository;

import com.examplatform.modules.evaluation.entity.EvaluationResult;
import com.examplatform.modules.evaluation.enums.EvaluationResultStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, String> {

    List<EvaluationResult> findByRunId(String runId);

    Page<EvaluationResult> findByRunId(String runId, Pageable pageable);

    List<EvaluationResult> findByRunIdAndStatus(String runId, EvaluationResultStatus status);

    Optional<EvaluationResult> findByRunIdAndQuestionId(String runId, String questionId);

    long countByRunIdAndStatus(String runId, EvaluationResultStatus status);

    long countByRunIdAndAnswerFoundTrue(String runId);

    long countByRunIdAndFromCacheTrue(String runId);

    @Transactional
    void deleteByRunId(String runId);

    // Aggregate metric গণনার জন্য (MetricsEngineService, Phase 8-এ ব্যবহৃত হবে)
    // row order: avg_similarity, avg_token_f1, avg_citation_precision, avg_citation_recall,
    //            avg_retrieval_latency, avg_llm_latency, avg_response_time,
    //            avg_token_input, avg_token_output, exact_match_count, total_count
    // নোট: single aggregate row হলেও Hibernate প্রতিটা row-কে নিজস্ব Object[]-এ wrap করে
    // এবং পুরো রেজাল্ট List<Object[]> হিসেবে রিটার্ন করে — তাই raw Object[] না নিয়ে
    // List<Object[]> নিয়ে প্রথম row বের করে আনতে হবে (নাহলে ArrayIndexOutOfBoundsException হয়)।
    @Query(value = """
        SELECT
            AVG(semantic_similarity_score),
            AVG(token_f1_score),
            AVG(citation_precision),
            AVG(citation_recall),
            AVG(retrieval_latency_ms),
            AVG(llm_latency_ms),
            AVG(response_time_ms),
            AVG(token_input),
            AVG(token_output),
            SUM(CASE WHEN exact_match = true THEN 1 ELSE 0 END),
            COUNT(*)
        FROM evaluation_result
        WHERE run_id = :runId
        """, nativeQuery = true)
    List<Object[]> findMetricAggregatesByRunId(@Param("runId") String runId);
}
