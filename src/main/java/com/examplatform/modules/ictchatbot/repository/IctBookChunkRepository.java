
package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctBookChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IctBookChunkRepository extends JpaRepository<IctBookChunk, String> {

    List<IctBookChunk> findBySourceUploadId(String sourceUploadId);

    List<IctBookChunk> findByWriterName(String writerName);

    @Transactional
    void deleteBySourceUploadId(String sourceUploadId);

    // Vector similarity search - cosine distance (lower = more similar)
    // :embedding কে string হিসেবে pass করতে হবে, format: "[0.1,0.2,0.3,...]"
    @Query(value = """
        SELECT * FROM ict_book_chunk c
        WHERE (:writerName IS NULL OR c.writer_name = :writerName)
        ORDER BY c.embedding <=> CAST(:embedding AS vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<IctBookChunk> findSimilarChunks(@Param("embedding") String embedding,
                                          @Param("writerName") String writerName,
                                          @Param("topK") int topK);

    // similarity score সহ দরকার হলে (distance হিসেবে) - debug/threshold check এর জন্য
    @Query(value = """
        SELECT c.*, (c.embedding <=> CAST(:embedding AS vector)) AS distance
        FROM ict_book_chunk c
        WHERE (:writerName IS NULL OR c.writer_name = :writerName)
        ORDER BY distance
        LIMIT :topK
        """, nativeQuery = true)
    List<Object[]> findSimilarChunksWithDistance(@Param("embedding") String embedding,
                                                  @Param("writerName") String writerName,
                                                  @Param("topK") int topK);
}
