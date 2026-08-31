package com.devforge.dto.project;

import com.devforge.dto.user.UserProfileResponse;

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
}
