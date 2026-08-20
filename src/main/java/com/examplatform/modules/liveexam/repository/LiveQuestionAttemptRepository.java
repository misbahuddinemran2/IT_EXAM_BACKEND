package com.examplatform.modules.liveexam.repository;

import com.examplatform.modules.liveexam.entity.LiveQuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiveQuestionAttemptRepository extends JpaRepository<LiveQuestionAttempt, String> {

    List<LiveQuestionAttempt> findByUserId(String userId);

    List<LiveQuestionAttempt> findByUserIdAndQuestionId(String userId, String questionId);

    List<LiveQuestionAttempt> findBySessionId(String sessionId);

    long countByQuestionIdAndIsCorrectTrue(String questionId);

    long countByQuestionIdAndIsCorrectFalse(String questionId);

    long countByQuestionIdAndIsSkippedTrue(String questionId);
}
