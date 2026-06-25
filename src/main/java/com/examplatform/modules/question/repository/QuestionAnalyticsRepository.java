package com.examplatform.modules.question.repository;

import com.examplatform.modules.question.entity.QuestionAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestionAnalyticsRepository
        extends JpaRepository<QuestionAnalytics, String> {

    Optional<QuestionAnalytics> findByQuestionId(String questionId);

    @Query("SELECT qa FROM QuestionAnalytics qa " +
           "WHERE qa.accuracyRate < :threshold " +
           "ORDER BY qa.accuracyRate ASC")
    List<QuestionAnalytics> findHardQuestions(
            double threshold);

    @Query("SELECT qa FROM QuestionAnalytics qa " +
           "ORDER BY qa.totalAttempts DESC")
    List<QuestionAnalytics> findMostAttempted();
}