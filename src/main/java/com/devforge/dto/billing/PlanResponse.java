package com.devforge.dto.billing;

import com.devforge.entity.enums.BillingPeriod;

public record PlanResponse(
        Long id,
        String name,
        Integer amountMinor,
        String currency,
        BillingPeriod billingPeriod,
        Integer billingInterval,
        Integer maxProjects,
        Integer maxTokensPerDay,
        Integer maxPreviews,
        Boolean unlimitedAi
) {
}
