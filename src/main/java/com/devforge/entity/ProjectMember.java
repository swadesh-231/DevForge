package com.devforge.entity;


import com.devforge.entity.enums.ProjectRole;
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
@Table(name = "project_members",
        indexes = {
                @Index(name = "idx_project_members_user_id", columnList = "user_id")
        }
)
public class ProjectMember {

    @EmbeddedId
    @EqualsAndHashCode.Include
    @ToString.Include
    private ProjectMemberId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("projectId")
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectRole projectRole;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant invitedAt;

    private Instant acceptedAt;
}
