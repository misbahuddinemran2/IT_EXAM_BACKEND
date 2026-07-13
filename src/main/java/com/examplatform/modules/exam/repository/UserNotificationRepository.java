package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, String> {

    List<UserNotification> findByUserIdOrderBySentAtDesc(String userId);

    List<UserNotification> findByUserIdAndIsReadFalseOrderBySentAtDesc(String userId);

    int countByUserIdAndIsReadFalse(String userId);

    void deleteByUserIdAndIsReadTrue(String userId);

    @Query("SELECT n FROM UserNotification n WHERE n.expiryDate IS NOT NULL AND n.expiryDate <= ?1")
    List<UserNotification> findExpiredNotifications(LocalDateTime now);

    @Query("SELECT n FROM UserNotification n ORDER BY n.sentAt DESC")
    List<UserNotification> findAllOrderBySentAtDesc();
}
