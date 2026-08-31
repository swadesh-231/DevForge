package com.devforge.dto.user;

import com.devforge.entity.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String imageUrl,
        UserRole role,
        Instant createdAt
) {
}
