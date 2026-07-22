package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctAnswerCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IctAnswerCacheRepository extends JpaRepository<IctAnswerCache, String> {

    // similarity > 0.95 মানে distance < 0.05 (cosine distance)
    @Query(value = """
        SELECT * FROM ict_answer_cache c
        WHERE (c.question_embedding <=> CAST(:embedding AS vector)) < :maxDistance
        ORDER BY c.question_embedding <=> CAST(:embedding AS vector)
        LIMIT 1
        """, nativeQuery = true)
    List<IctAnswerCache> findClosestMatch(@Param("embedding") String embedding,
                                           @Param("maxDistance") double maxDistance);

    // ===================================
    // DEBUG / TUNING মেথড — cache threshold টিউন করার জন্য।
    // Threshold ছাড়াই top N সবচেয়ে কাছের প্রশ্ন + তাদের distance দেখায়।
    // Tuning শেষ হলে এই মেথডটা মুছে ফেলা যাবে।
    // ===================================
    @Query(value = """
        SELECT c.question_text, (c.question_embedding <=> CAST(:embedding AS vector)) as dist
        FROM ict_answer_cache c
        ORDER BY dist
        LIMIT :topN
        """, nativeQuery = true)
    List<Object[]> findTopClosestForDebug(@Param("embedding") String embedding,
                                           @Param("topN") int topN);

}
