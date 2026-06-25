package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.UserPerformanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPerformanceSummaryRepository extends JpaRepository<UserPerformanceSummary, String> {

    Optional<UserPerformanceSummary> findByUserId(String userId);
}