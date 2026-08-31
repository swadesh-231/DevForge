package com.devforge.dto.billing;

import com.devforge.entity.Plan;
import com.devforge.entity.enums.BillingPeriod;

public record PlanResponse(
        Long id,
        String name,
        Integer amountInPaise,
        String currency,
        BillingPeriod billingPeriod,
        Integer billingInterval,
        Integer maxProjects,
        Integer maxTokensPerDay,
        Integer maxPreview,
        Boolean unlimitedAi
) {
    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getAmountInPaise(),
                plan.getCurrency(),
                plan.getBillingPeriod(),
                plan.getBillingInterval(),
                plan.getMaxProjects(),
                plan.getMaxTokensPerDay(),
                plan.getMaxPreview(),
                plan.getUnlimitedAi()
        );
    }
}
