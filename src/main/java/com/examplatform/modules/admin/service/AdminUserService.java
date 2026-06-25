
package com.examplatform.modules.admin.service;

import com.examplatform.modules.admin.dto.AdminUserResponse;
import com.examplatform.modules.admin.dto.GrantSubscriptionRequest;
import com.examplatform.modules.exam.repository.ExamSessionRepository;
import com.examplatform.modules.subscription.entity.SubscriptionPlan;
import com.examplatform.modules.subscription.entity.UserSubscription;
import com.examplatform.modules.subscription.repository.SubscriptionPlanRepository;
import com.examplatform.modules.subscription.repository.UserSubscriptionRepository;
import com.examplatform.modules.user.entity.User;
import com.examplatform.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final ExamSessionRepository examSessionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    public Page<AdminUserResponse> getAllUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<User> users = (keyword != null && !keyword.isBlank())
                ? userRepository.searchUsers(keyword)
                : userRepository.findAll();

        List<AdminUserResponse> responses = users.stream()
                .map(this::toResponse)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responses.size());

        if (start >= responses.size()) {
            return Page.empty(pageable);
        }

        return new PageImpl<>(
                responses.subList(start, end),
                pageable,
                responses.size()
        );
    }

    public AdminUserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    public void toggleUserStatus(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public void grantSubscription(String userId, GrantSubscriptionRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanCode(req.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found: " + req.getPlanId()));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusDays(
                req.getDurationDays() > 0 ? req.getDurationDays() : plan.getDurationDays()
        );

        UserSubscription subscription = UserSubscription.builder()
                .userId(user.getId())
                .plan(plan)
                .status(UserSubscription.Status.ACTIVE)
                .paymentMethod(UserSubscription.PaymentMethod.ADMIN_GRANTED)
                .startsAt(now)
                .expiresAt(expiry)
                .amountPaid(0)
                .discountAmount(0)
                .notes(req.getNotes() != null ? req.getNotes() : "Admin কর্তৃক প্রদত্ত")
                .build();

        userSubscriptionRepository.save(subscription);
    }

    private AdminUserResponse toResponse(User u) {
        String subStatus = "NONE";
        String subExpiry = null;
        String subPlanName = null;

        var activeSub = userSubscriptionRepository
                .findTopByUserIdAndStatusAndExpiresAtAfter(
                        u.getId(),
                        UserSubscription.Status.ACTIVE,
                        LocalDateTime.now());

        if (activeSub.isPresent()) {
            subStatus = "ACTIVE";
            subExpiry = activeSub.get().getExpiresAt() != null
                    ? activeSub.get().getExpiresAt().format(FMT) : null;
            subPlanName = activeSub.get().getPlan() != null
                    ? activeSub.get().getPlan().getNameBn() : null;
        } else {
            var trialSub = userSubscriptionRepository
                    .findTopByUserIdAndStatusAndExpiresAtAfter(
                            u.getId(),
                            UserSubscription.Status.TRIAL,
                            LocalDateTime.now());
            if (trialSub.isPresent()) {
                subStatus = "TRIAL";
                subExpiry = trialSub.get().getExpiresAt() != null
                        ? trialSub.get().getExpiresAt().format(FMT) : null;
                subPlanName = trialSub.get().getPlan() != null
                        ? trialSub.get().getPlan().getNameBn() : null;
            }
        }

        long totalExams = examSessionRepository.countByUserId(u.getId());

        return AdminUserResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .isActive(u.isActive())
                .createdAt(u.getCreatedAt() != null
                        ? u.getCreatedAt().format(FMT) : "")
                .lastLoginAt(u.getLastLoginAt() != null
                        ? u.getLastLoginAt().format(FMT) : "Never")
                .subscriptionStatus(subStatus)
                .subscriptionExpiry(subExpiry)
                .subscriptionPlanName(subPlanName)
                .totalExams(totalExams)
                .build();
    }
}
