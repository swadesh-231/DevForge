package com.devforge.dto.billing;

public record CheckoutResponse(
        String checkoutSessionId,
        String checkoutUrl,
        String planName,
        Integer amountMinor,
        String currency
) {
}
