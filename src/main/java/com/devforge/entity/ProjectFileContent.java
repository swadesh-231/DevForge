package com.devforge.entity;

import jakarta.persistence.*;
import lombok.*;
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
@Table(name = "project_file_contents")
public class ProjectFileContent {
    @Id
    @Column(length = 640)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String storageKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
