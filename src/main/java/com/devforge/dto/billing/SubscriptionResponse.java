package com.devforge.dto.billing;

import com.devforge.entity.Subscription;
import com.devforge.entity.enums.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscriptionResponse(
        Long id,
        PlanResponse plan,
        SubscriptionStatus status,
        String razorpaySubscriptionId,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        Instant chargeAt,
        Integer paidCount,
        Integer remainingCount,
        Boolean cancelAtPeriodEnd,
        Instant cancelledAt
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                PlanResponse.from(subscription.getPlan()),
                subscription.getStatus(),
                subscription.getRazorpaySubscriptionId(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.getChargeAt(),
                subscription.getPaidCount(),
                subscription.getRemainingCount(),
                subscription.getCancelAtPeriodEnd(),
                subscription.getCancelledAt()
        );
    }
}
