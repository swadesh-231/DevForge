package com.devforge.security.jwt;

import com.devforge.entity.User;

public interface JwtService {
    String generateAccessToken(User user);

    Long extractUserId(String token);

    String extractEmail(String token);
}
