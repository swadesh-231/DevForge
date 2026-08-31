package com.devforge.entity;

import com.devforge.entity.enums.PreviewStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "previews",
        indexes = {
                @Index(name = "idx_previews_project_id", columnList = "project_id"),
                @Index(name = "idx_previews_status_last_accessed", columnList = "status, last_accessed_at")
        }
)
public class Preview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @Column(length = 128)
    private String namespace;

    @Column(length = 253)
    private String podName;

    @Column(length = 512)
    @ToString.Include
    private String previewUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ToString.Include
    private PreviewStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Instant startedAt;

    private Instant endedAt;


    private Instant lastAccessedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
