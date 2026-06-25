package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, String> {

    List<UserNotification> findByUserIdOrderBySentAtDesc(String userId);

    List<UserNotification> findByUserIdAndIsReadFalseOrderBySentAtDesc(String userId);

    int countByUserIdAndIsReadFalse(String userId);

    void deleteByUserIdAndIsReadTrue(String userId);
}