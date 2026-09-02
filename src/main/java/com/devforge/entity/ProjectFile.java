package com.devforge.entity;

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
@Table(name = "project_files",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_files_project_path",
                        columnNames = {"project_id", "path"}
                )
        },
        indexes = {
                @Index(name = "idx_project_files_project_id", columnList = "project_id")
        }
)
public class ProjectFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @Column(nullable = false, length = 512)
    @ToString.Include
    private String path;

    @Column(nullable = false, unique = true, length = 640)
    private String storageKey;

    @Column(length = 64)
    private String contentHash;

    @Builder.Default
    @Column(nullable = false)
    private Long sizeBytes = 0L;

    @Column(length = 128)
    private String mimeType;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
