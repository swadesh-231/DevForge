package com.devforge.entity.enums;

/**
 * Mirrors Razorpay's subscription lifecycle, not Stripe's.
 * HALTED is Razorpay's "retries exhausted" (Stripe's past_due);
 * PENDING is a failed charge still being retried.
 */
public enum SubscriptionStatus {
    CREATED,
    AUTHENTICATED,
    ACTIVE,
    PENDING,
    HALTED,
    CANCELLED,
    COMPLETED,
    EXPIRED,
    PAUSED
}
