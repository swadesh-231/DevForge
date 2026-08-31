package com.devforge.dto.billing;


public record CheckoutResponse(
        String razorpayKeyId,
        String razorpaySubscriptionId,
        String shortUrl,
        String planName,
        Integer amountInPaise,
        String currency
) {
}
