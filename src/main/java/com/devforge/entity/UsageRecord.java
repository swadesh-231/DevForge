package com.devforge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "usage_records",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_usage_user_date",
            columnNames = {"user_id", "date"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class UsageRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    @ToString.Include
    private LocalDate date;

    @Builder.Default
    @Column(nullable = false)
    private Integer tokensIn = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer tokensOut = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer messageCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer previewSeconds = 0;
}
