//package com.examplatform.modules.exam.repository;
//
//import com.examplatform.modules.exam.entity.QuestionAttempt;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, String> {
//
//    List<QuestionAttempt> findBySessionId(String sessionId);
//
//    Optional<QuestionAttempt> findBySessionIdAndQuestionId(
//            String sessionId, String questionId);
//
//    long countBySessionIdAndIsCorrectTrue(String sessionId);
//}