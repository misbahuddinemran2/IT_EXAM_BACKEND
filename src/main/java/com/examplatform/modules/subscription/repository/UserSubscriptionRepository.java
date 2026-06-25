package com.examplatform.modules.subscription.repository;

import com.examplatform.modules.subscription.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, String> {

    Optional<UserSubscription> findTopByUserIdAndStatusAndExpiresAtAfter(
            String userId, UserSubscription.Status status, LocalDateTime now);

    Optional<UserSubscription> findTopByUserIdAndStatusInAndExpiresAtAfter(
            String userId, List<UserSubscription.Status> statuses, LocalDateTime now);

    List<UserSubscription> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByStatus(UserSubscription.Status status);
}