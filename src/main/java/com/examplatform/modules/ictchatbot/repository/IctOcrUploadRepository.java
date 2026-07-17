package com.examplatform.modules.ictchatbot.repository;

import com.examplatform.modules.ictchatbot.entity.IctOcrUpload;
import com.examplatform.modules.ictchatbot.enums.IctUploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IctOcrUploadRepository extends JpaRepository<IctOcrUpload, String> {

    List<IctOcrUpload> findByStatusOrderByCreatedAtAsc(IctUploadStatus status);

    List<IctOcrUpload> findByWriterNameAndStatus(String writerName, IctUploadStatus status);

    @Query("SELECT u FROM IctOcrUpload u WHERE u.subjectId = :subjectId AND u.status = :status")
    List<IctOcrUpload> findBySubjectAndStatus(@Param("subjectId") String subjectId,
                                               @Param("status") IctUploadStatus status);
}
