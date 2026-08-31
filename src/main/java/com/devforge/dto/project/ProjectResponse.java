package com.devforge.dto.project;

import com.devforge.dto.user.UserProfileResponse;
import com.devforge.entity.Project;

import java.time.Instant;

public record ProjectResponse(
        Long id,
        String name,
        String slug,
        String description,
        Boolean isPublic,
        UserProfileResponse owner,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getSlug(),
                project.getDescription(),
                project.getIsPublic(),
                UserProfileResponse.from(project.getOwner()),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
