package com.examplatform.modules.question.repository;

import com.examplatform.modules.question.entity.BulkUploadJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BulkUploadJobRepository
        extends JpaRepository<BulkUploadJob, String> {

    Page<BulkUploadJob> findAllByOrderByStartedAtDesc(
            Pageable pageable);
}