package com.devforge.service;

import com.devforge.dto.billing.PlanLimitsResponse;
import com.devforge.dto.billing.SubscriptionResponse;
import com.devforge.entity.enums.SubscriptionStatus;

import java.time.Instant;
import java.util.Optional;

public interface SubscriptionService {

    Optional<SubscriptionResponse> getCurrentSubscription(Long userId);

    PlanLimitsResponse getEffectiveLimits(Long userId);

    boolean canCreateProject(Long userId);

    void activateSubscription(Long userId, Long planId, String stripeSubscriptionId, String stripeCustomerId);

    void updateSubscription(String stripeSubscriptionId,
                            SubscriptionStatus status,
                            Instant periodStart,
                            Instant periodEnd,
                            Boolean cancelAtPeriodEnd,
                            Long planId);

    void renewSubscriptionPeriod(String stripeSubscriptionId, Instant periodStart, Instant periodEnd);

    void markSubscriptionPastDue(String stripeSubscriptionId);

    void cancelSubscription(String stripeSubscriptionId);
}
