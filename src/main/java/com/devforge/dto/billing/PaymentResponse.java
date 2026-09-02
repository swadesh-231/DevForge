package com.devforge.dto.billing;

import com.devforge.entity.enums.PaymentMethod;
import com.devforge.entity.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        Long id,
        String stripePaymentIntentId,
        String stripeInvoiceId,
        Integer amountMinor,
        String currency,
        PaymentStatus status,
        PaymentMethod method,
        String failureMessage,
        String receiptUrl,
        Instant paidAt,
        Instant createdAt
) {
}
