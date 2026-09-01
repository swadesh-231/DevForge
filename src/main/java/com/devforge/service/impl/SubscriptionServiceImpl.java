package com.devforge.service.impl;

import com.devforge.dto.billing.SubscriptionResponse;
import com.devforge.entity.enums.SubscriptionStatus;
import com.devforge.service.SubscriptionService;

import java.time.Instant;

public class SubscriptionServiceImpl implements SubscriptionService {
    @Override
    public SubscriptionResponse getCurrentSubscription() {
        return null;
    }

    @Override
    public void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId) {

    }

    @Override
    public void updateSubscription(String gatewaySubscriptionId, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId) {

    }

    @Override
    public void cancelSubscription(String gatewaySubscriptionId) {

    }

    @Override
    public void renewSubscriptionPeriod(String subId, Instant periodStart, Instant periodEnd) {

    }

    @Override
    public void markSubscriptionPastDue(String subId) {

    }

    @Override
    public boolean canCreateNewProject() {
        return false;
    }
}
