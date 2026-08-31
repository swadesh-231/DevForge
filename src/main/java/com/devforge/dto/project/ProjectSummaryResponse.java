package com.devforge.dto.project;

import com.devforge.entity.enums.ProjectRole;

import java.time.Instant;


public record ProjectSummaryResponse(
        Long id,
        String name,
        String slug,
        Boolean isPublic,
        ProjectRole role,
        Instant createdAt,
        Instant updatedAt
) {
}
