package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, String> {

    // Exam এর সব question (order অনুযায়ী)
    List<ExamQuestion> findByExamIdOrderByOrderNumberAsc(String examId);

    // Exam এ কতটা question আছে
    long countByExamId(String examId);

    // Question already আছে কিনা check
    boolean existsByExamIdAndQuestionId(String examId, String questionId);

    // Exam delete হলে সব question delete
    void deleteByExamId(String examId);

    // Question IDs শুধু (session এর জন্য)
    @Query("SELECT eq.questionId FROM ExamQuestion eq " +
            "WHERE eq.examId = :examId " +
            "ORDER BY eq.orderNumber ASC")
    List<String> findQuestionIdsByExamId(@Param("examId") String examId);

    // Shuffled question IDs (shuffle_questions = true হলে)
    @Query(value = "SELECT eq.question_id FROM exam_questions eq " +
            "WHERE eq.exam_id = :examId " +
            "ORDER BY RANDOM()", nativeQuery = true)
    List<String> findQuestionIdsByExamIdShuffled(@Param("examId") String examId);
}