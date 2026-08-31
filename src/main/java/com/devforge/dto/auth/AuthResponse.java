package com.devforge.dto.auth;

import com.devforge.dto.user.UserProfileResponse;
import lombok.Builder;

import java.time.Instant;

@Builder
public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UserProfileResponse user
) {
}
