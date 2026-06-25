package com.examplatform.modules.subscription.service;

import com.examplatform.modules.subscription.entity.SubscriptionPlan;
import com.examplatform.modules.subscription.entity.UserSubscription;
import com.examplatform.modules.subscription.repository.SubscriptionPlanRepository;
import com.examplatform.modules.subscription.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    // ─── Active Subscription পাওয়া ────────────────────────────
    public Optional<UserSubscription> getActiveSubscription(String userId) {
        return userSubscriptionRepository
                .findTopByUserIdAndStatusInAndExpiresAtAfter(
                        userId,
                        List.of(UserSubscription.Status.ACTIVE,
                                UserSubscription.Status.TRIAL),
                        LocalDateTime.now()
                );
    }

    // ─── User এর Plan Type পাওয়া ──────────────────────────────
    public SubscriptionPlan.PlanType getUserPlanType(String userId) {
        return getActiveSubscription(userId)
                .map(sub -> sub.getPlan().getPlanType())
                .orElse(SubscriptionPlan.PlanType.FREE);
    }

    // ─── Active Plan Features পাওয়া ───────────────────────────
    public SubscriptionPlan getActivePlan(String userId) {
        return getActiveSubscription(userId)
                .map(UserSubscription::getPlan)
                .orElseGet(() -> subscriptionPlanRepository
                        .findByPlanCode("FREE")
                        .orElseThrow(() ->
                            new RuntimeException("FREE plan পাওয়া যায়নি")));
    }

    // ─── সব Active Plans পাওয়া ────────────────────────────────
    public List<SubscriptionPlan> getAllActivePlans() {
        return subscriptionPlanRepository
                .findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    // ─── Trial Subscription তৈরি ──────────────────────────────
    @Transactional
    public void createTrialSubscription(String userId) {
        SubscriptionPlan monthlyPlan = subscriptionPlanRepository
                .findByPlanCode("MONTHLY")
                .orElseThrow(() ->
                    new RuntimeException("MONTHLY plan পাওয়া যায়নি"));

        UserSubscription trial = UserSubscription.builder()
                .userId(userId)
                .plan(monthlyPlan)
                .status(UserSubscription.Status.TRIAL)
                .paymentMethod(UserSubscription.PaymentMethod.ADMIN_GRANTED)
                .startsAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .amountPaid(0)
                .discountAmount(0)
                .notes("New user 7 days free trial")
                .build();

        userSubscriptionRepository.save(trial);
    }

    // ─── Admin Grant Subscription ──────────────────────────────
    @Transactional
    public void grantSubscription(String userId, String planCode,
                                   int durationDays, String adminId,
                                   String notes) {

        SubscriptionPlan plan = subscriptionPlanRepository
                .findByPlanCode(planCode)
                .orElseThrow(() ->
                    new RuntimeException(planCode + " plan পাওয়া যায়নি"));

        // আগের active/trial subscription cancel করা
        getActiveSubscription(userId).ifPresent(sub -> {
            sub.setStatus(UserSubscription.Status.CANCELLED);
            userSubscriptionRepository.save(sub);
        });

        // নতুন subscription তৈরি
        UserSubscription subscription = UserSubscription.builder()
                .userId(userId)
                .plan(plan)
                .status(UserSubscription.Status.ACTIVE)
                .paymentMethod(UserSubscription.PaymentMethod.ADMIN_GRANTED)
                .startsAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(durationDays))
                .grantedBy(adminId)
                .amountPaid(0)
                .discountAmount(0)
                .notes(notes)
                .build();

        userSubscriptionRepository.save(subscription);
    }

    // ─── Subscription Expire করা ───────────────────────────────
    @Transactional
    public void expireSubscription(String userId) {
        getActiveSubscription(userId).ifPresent(sub -> {
            sub.setStatus(UserSubscription.Status.EXPIRED);
            userSubscriptionRepository.save(sub);
        });
    }

    // ─── User এর সব Subscription History ─────────────────────
    public List<UserSubscription> getSubscriptionHistory(String userId) {
        return userSubscriptionRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ─── Subscription আছে কিনা check ──────────────────────────
    public boolean hasActiveSubscription(String userId) {
        return getActiveSubscription(userId).isPresent();
    }

    // ─── Plan upgrade check ────────────────────────────────────
    public boolean isPaidUser(String userId) {
        SubscriptionPlan.PlanType planType = getUserPlanType(userId);
        return planType == SubscriptionPlan.PlanType.MONTHLY ||
               planType == SubscriptionPlan.PlanType.YEARLY ||
               planType == SubscriptionPlan.PlanType.CUSTOM;
    }
}