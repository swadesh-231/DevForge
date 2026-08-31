package com.devforge.entity;

import com.devforge.entity.enums.PaymentMethod;
import com.devforge.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_payments_user_id", columnList = "user_id"),
                @Index(name = "idx_payments_subscription_id", columnList = "subscription_id"),
                @Index(name = "idx_payments_status", columnList = "status")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name = "razorpay_payment_id", unique = true, length = 64)
    @ToString.Include
    private String razorpayPaymentId;

    @Column(name = "razorpay_order_id", length = 64)
    private String razorpayOrderId;

    @Column(name = "razorpay_invoice_id", length = 64)
    private String razorpayInvoiceId;

    @Column(length = 256)
    private String razorpaySignature;

    @Column(nullable = false)
    private Integer amountInPaise;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ToString.Include
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod method;

    @Column(length = 64)
    private String errorCode;

    @Column(columnDefinition = "TEXT")
    private String errorDescription;

    private Instant capturedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
