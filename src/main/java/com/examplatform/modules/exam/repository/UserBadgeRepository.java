package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, String> {

    List<UserBadge> findByUserId(String userId);

    Optional<UserBadge> findByUserIdAndBadgeType(String userId, UserBadge.BadgeType badgeType);

    int countByUserId(String userId);
}