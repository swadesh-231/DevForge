package com.devforge.dto.billing;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSubscriptionRequest(

        @NotNull(message = "Plan id is required")
        @Positive(message = "Plan id must be positive")
        Long planId

) {
}
