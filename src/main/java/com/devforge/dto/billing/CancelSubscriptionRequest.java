package com.devforge.dto.billing;

import jakarta.validation.constraints.NotNull;


public record CancelSubscriptionRequest(

        @NotNull(message = "atCycleEnd is required")
        Boolean atCycleEnd

) {
}
