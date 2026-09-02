package com.devforge.dto.billing;

import com.devforge.entity.enums.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionResponse(
        Long id,
        PlanResponse plan,
        SubscriptionStatus status,
        String stripeSubscriptionId,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        Instant trialEndsAt,
        Boolean cancelAtPeriodEnd,
        Instant canceledAt
) {
}
