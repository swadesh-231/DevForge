package com.devforge.entity;

import com.devforge.entity.enums.SubscriptionStatus;
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
@Table(name = "subscriptions",
        indexes = {
                @Index(name = "idx_subscriptions_user_id", columnList = "user_id"),
                @Index(name = "idx_subscriptions_plan_id", columnList = "plan_id"),
                @Index(name = "idx_subscriptions_status", columnList = "status")
        }
)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, name = "plan_id")
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ToString.Include
    private SubscriptionStatus status;


    @Column(name = "razorpay_subscription_id", unique = true, length = 64)
    @ToString.Include
    private String razorpaySubscriptionId;

    @Column(name = "razorpay_customer_id", length = 64)
    private String razorpayCustomerId;

    @Column(length = 512)
    private String shortUrl;


    private Integer totalCount;

    @Builder.Default
    @Column(nullable = false)
    private Integer paidCount = 0;

    private Integer remainingCount;

    @Builder.Default
    @Column(nullable = false)
    private Integer authAttempts = 0;

    private Instant chargeAt;

    private Instant currentPeriodStart;

    private Instant currentPeriodEnd;

    @Builder.Default
    @Column(nullable = false)
    private Boolean cancelAtPeriodEnd = false;

    private Instant cancelledAt;

    private Instant endedAt;


    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
