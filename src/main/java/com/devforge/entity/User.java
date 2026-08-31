package com.devforge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_razorpay_customer_id", columnList = "razorpay_customer_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    @ToString.Include
    private String email;

    @Column(nullable = false, length = 100)
    private String password;


    @Column(name = "razorpay_customer_id", unique = true, length = 64)
    private String razorpayCustomerId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant deletedAt;
}
