package com.devforge.security.jwt;

import com.devforge.entity.User;

import java.time.Instant;

public interface JwtService {
    IssuedToken generateAccessToken(User user);

    IssuedToken generateRefreshToken(User user);

    Long extractUserId(String accessToken);

    String extractEmail(String accessToken);

    RefreshTokenClaims parseRefreshToken(String refreshToken);

    record IssuedToken(String value, Instant issuedAt, Instant expiresAt) {
    }

    record RefreshTokenClaims(Long userId, String email, String tokenId, Instant expiresAt) {
    }
}
