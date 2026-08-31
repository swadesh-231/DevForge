package com.devforge.dto.billing;

import com.devforge.entity.Payment;
import com.devforge.entity.enums.PaymentMethod;
import com.devforge.entity.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        Long id,
        String razorpayPaymentId,
        String razorpayInvoiceId,
        Integer amountInPaise,
        String currency,
        PaymentStatus status,
        PaymentMethod method,
        String errorDescription,
        Instant capturedAt,
        Instant createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getRazorpayPaymentId(),
                payment.getRazorpayInvoiceId(),
                payment.getAmountInPaise(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getErrorDescription(),
                payment.getCapturedAt(),
                payment.getCreatedAt()
        );
    }
}
