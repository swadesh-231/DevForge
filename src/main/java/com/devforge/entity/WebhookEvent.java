package com.devforge.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "webhook_events",
        indexes = {
                @Index(name = "idx_webhook_events_processed_at", columnList = "processed_at"),
                @Index(name = "idx_webhook_events_event", columnList = "event")
        }
)
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;


    @Column(name = "razorpay_event_id", nullable = false, unique = true, length = 64)
    @ToString.Include
    private String razorpayEventId;


    @Column(nullable = false, length = 64)
    @ToString.Include
    private String event;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Builder.Default
    @Column(nullable = false)
    private Boolean signatureValid = false;

    private Instant processedAt;

    @Column(columnDefinition = "TEXT")
    private String error;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
