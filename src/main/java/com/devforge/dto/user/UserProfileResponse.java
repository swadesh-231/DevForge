package com.devforge.dto.user;

import com.devforge.entity.User;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String email,
        String name,
        Instant createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(), user.getEmail(), user.getName(), user.getCreatedAt());
    }
}
