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

    @Column(name = "stripe_payment_intent_id", unique = true, length = 64)
    @ToString.Include
    private String stripePaymentIntentId;

    @Column(name = "stripe_invoice_id", length = 64)
    private String stripeInvoiceId;

    @Column(name = "stripe_charge_id", length = 64)
    private String stripeChargeId;

    @Column(nullable = false)
    private Integer amountMinor;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @ToString.Include
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod method;

    @Column(length = 64)
    private String failureCode;

    @Column(columnDefinition = "TEXT")
    private String failureMessage;

    @Column(length = 512)
    private String receiptUrl;

    private Instant paidAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
