package com.devforge.service;

import com.devforge.dto.billing.SubscriptionResponse;
import com.devforge.entity.enums.SubscriptionStatus;

import java.time.Instant;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription();
    void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId);
    void updateSubscription(String gatewaySubscriptionId, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId);
    void cancelSubscription(String gatewaySubscriptionId);
    void renewSubscriptionPeriod(String subId, Instant periodStart, Instant periodEnd);
    void markSubscriptionPastDue(String subId);
    boolean canCreateNewProject();
}
