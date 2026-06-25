package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.UserTopicWeakness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTopicWeaknessRepository extends JpaRepository<UserTopicWeakness, String> {



    List<UserTopicWeakness> findByUserIdAndExamTypeIdOrderByWeaknessScoreDesc(String userId, String examTypeId);

    // এই methods যোগ করুন:

    List<UserTopicWeakness> findByUserIdOrderByAccuracyRateDesc(String userId);

    List<UserTopicWeakness> findByUserIdOrderByWeaknessScoreDesc(String userId);

    Optional<UserTopicWeakness> findByUserIdAndTopicId(String userId, String topicId);

    @Query("SELECT w FROM UserTopicWeakness w WHERE w.userId = ?1 AND w.weaknessScore > 0.6 ORDER BY w.weaknessScore DESC")
    List<UserTopicWeakness> findWeakAreasForUser(String userId);


}