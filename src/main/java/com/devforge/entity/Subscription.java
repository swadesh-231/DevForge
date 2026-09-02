package com.devforge.entity;

import com.devforge.entity.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

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

    public static final Set<SubscriptionStatus> ENTITLING_STATUSES = EnumSet.of(
            SubscriptionStatus.ACTIVE,
            SubscriptionStatus.TRIALING,
            SubscriptionStatus.PAST_DUE
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @ToString.Include
    private SubscriptionStatus status;

    @Column(name = "stripe_subscription_id", unique = true, length = 64)
    @ToString.Include
    private String stripeSubscriptionId;

    @Column(name = "stripe_customer_id", length = 64)
    private String stripeCustomerId;

    @Column(name = "stripe_price_id", length = 64)
    private String stripePriceId;

    private Instant currentPeriodStart;

    private Instant currentPeriodEnd;

    private Instant trialEndsAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean cancelAtPeriodEnd = false;

    private Instant canceledAt;

    private Instant endedAt;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public boolean isEntitling() {
        return ENTITLING_STATUSES.contains(status);
    }
}
