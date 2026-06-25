package com.examplatform.modules.subscription;

import com.examplatform.common.exception.ValidationException;
import com.examplatform.modules.subscription.service.SubscriptionService;
import com.examplatform.modules.subscription.entity.SubscriptionPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionCheckHelper {

    private final SubscriptionService subscriptionService;

    // Plan পাওয়া
    public SubscriptionPlan getActivePlan(String userId) {
        return subscriptionService.getActivePlan(userId);
    }

    // Battle exam করতে পারবে?
    public void checkBattleAccess(String userId) {
        SubscriptionPlan plan = getActivePlan(userId);
        if (!plan.isHasBattleExam()) {
            throw new ValidationException(
                "১ vs ১ Battle Exam এর জন্য Yearly Plan প্রয়োজন");
        }
    }

    // Written exam করতে পারবে?
    public void checkWrittenAccess(String userId) {
        SubscriptionPlan plan = getActivePlan(userId);
        if (!plan.isHasWrittenExam()) {
            throw new ValidationException(
                "Written Exam এর জন্য Yearly Plan প্রয়োজন");
        }
    }

    // Live exam করতে পারবে?
    public void checkLiveExamAccess(String userId) {
        SubscriptionPlan plan = getActivePlan(userId);
        if (!plan.isHasLiveExam()) {
            throw new ValidationException(
                "Live Exam এর জন্য Yearly Plan প্রয়োজন");
        }
    }

    // Special exam access check (admin defined required_plan)
    public void checkSpecialExamAccess(String userId,
                                        SubscriptionPlan.PlanType requiredPlan) {
        SubscriptionPlan.PlanType userPlan =
                subscriptionService.getUserPlanType(userId);

        int userLevel = planLevel(userPlan);
        int requiredLevel = planLevel(requiredPlan);

        if (userLevel < requiredLevel) {
            throw new ValidationException(
                "এই Exam এর জন্য " + requiredPlan.name() + " Plan প্রয়োজন");
        }
    }

    // Mock test limit check
    public void checkMockTestLimit(String userId, int usedThisMonth) {
        SubscriptionPlan plan = getActivePlan(userId);
        int limit = plan.getMaxMockTestsPerMonth();
        if (limit != -1 && usedThisMonth >= limit) {
            throw new ValidationException(
                "এই মাসে Mock Test এর limit শেষ হয়ে গেছে। Upgrade করুন।");
        }
    }

    // Practice limit check
    public void checkPracticeLimit(String userId, int usedToday) {
        SubscriptionPlan plan = getActivePlan(userId);
        int limit = plan.getMaxPracticePerDay();
        if (limit != -1 && usedToday >= limit) {
            throw new ValidationException(
                "আজকের Practice limit শেষ হয়ে গেছে। আগামীকাল আবার চেষ্টা করুন।");
        }
    }

    // Plan level — hierarchy check এর জন্য
    private int planLevel(SubscriptionPlan.PlanType planType) {
        return switch (planType) {
            case FREE -> 0;
            case MONTHLY -> 1;
            case YEARLY -> 2;
            case CUSTOM -> 3;
        };
    }
}