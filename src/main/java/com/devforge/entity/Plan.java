package com.devforge.entity;

import com.devforge.entity.enums.BillingPeriod;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @NotBlank(message = "Plan name is required")
    @Size(max = 50, message = "Plan name cannot exceed 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    @ToString.Include
    private String name;

    @Column(name = "stripe_product_id", length = 64)
    private String stripeProductId;

    @Column(name = "stripe_price_id", unique = true, length = 64)
    @ToString.Include
    private String stripePriceId;

    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount cannot be negative")
    @Builder.Default
    @Column(nullable = false)
    private Integer amountMinor = 0;

    @NotBlank
    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 10)
    private BillingPeriod billingPeriod = BillingPeriod.MONTHLY;

    @NotNull
    @Min(value = 1, message = "Billing interval must be at least 1")
    @Builder.Default
    @Column(nullable = false)
    private Integer billingInterval = 1;

    @NotNull(message = "Maximum projects is required")
    @Min(value = 0, message = "Maximum projects cannot be negative")
    @Column(nullable = false)
    private Integer maxProjects;

    @NotNull(message = "Maximum tokens per day is required")
    @Min(value = 0, message = "Maximum tokens per day cannot be negative")
    @Column(nullable = false)
    private Integer maxTokensPerDay;

    @NotNull(message = "Maximum previews is required")
    @Min(value = 0, message = "Maximum previews cannot be negative")
    @Column(nullable = false)
    private Integer maxPreviews;

    @NotNull(message = "Unlimited AI value is required")
    @Builder.Default
    @Column(nullable = false)
    private Boolean unlimitedAi = false;

    @NotNull(message = "Active status is required")
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(nullable = false)
    private Integer displayOrder = 0;
}
