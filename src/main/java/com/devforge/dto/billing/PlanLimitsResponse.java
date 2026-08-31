package com.devforge.dto.billing;

public record PlanLimitsResponse(
        String planName,
        Integer maxProjects,
        Integer maxTokensPerDay,
        Integer maxPreview,
        Boolean unlimitedAi
) {
}
