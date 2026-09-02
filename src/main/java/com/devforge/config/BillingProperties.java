package com.devforge.config;

import com.devforge.dto.billing.PlanLimitsResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.billing")
public record BillingProperties(

        @NotBlank
        @DefaultValue("Free")
        String freePlanName,

        @Min(0)
        @DefaultValue("3")
        int freeMaxProjects,

        @Min(0)
        @DefaultValue("50000")
        int freeMaxTokensPerDay,

        @Min(0)
        @DefaultValue("1")
        int freeMaxPreviews

) {
    public PlanLimitsResponse freeLimits() {
        return new PlanLimitsResponse(
                freePlanName, freeMaxProjects, freeMaxTokensPerDay, freeMaxPreviews, false);
    }
}
