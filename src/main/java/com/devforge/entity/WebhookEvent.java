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
                @Index(name = "idx_webhook_events_type", columnList = "type")
        }
)
public class WebhookEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "stripe_event_id", nullable = false, unique = true, length = 64)
    @ToString.Include
    private String stripeEventId;

    @Column(nullable = false, length = 64)
    @ToString.Include
    private String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    private Instant processedAt;

    @Column(columnDefinition = "TEXT")
    private String error;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
