package com.devforge.dto.billing;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyPaymentRequest(

        @NotBlank(message = "Payment id is required")
        @Size(max = 64, message = "Payment id cannot exceed 64 characters")
        @JsonProperty("razorpay_payment_id")
        String razorpayPaymentId,

        @NotBlank(message = "Subscription id is required")
        @Size(max = 64, message = "Subscription id cannot exceed 64 characters")
        @JsonProperty("razorpay_subscription_id")
        String razorpaySubscriptionId,

        @NotBlank(message = "Signature is required")
        @Size(max = 256, message = "Signature cannot exceed 256 characters")
        @JsonProperty("razorpay_signature")
        String razorpaySignature

) {
}
