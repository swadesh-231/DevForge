package com.devforge.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(

        @NotBlank
        @Size(min = 32, message = "JWT secret must be at least 32 characters for HS256")
        String secret,

        @NotBlank
        String issuer,

        @NotNull
        Duration accessTokenTtl,

        @NotNull
        Duration refreshTokenTtl

) {
    public JwtProperties {
        if (accessTokenTtl != null && (accessTokenTtl.isZero() || accessTokenTtl.isNegative())) {
            throw new IllegalArgumentException("security.jwt.access-token-ttl must be positive");
        }
        if (refreshTokenTtl != null && (refreshTokenTtl.isZero() || refreshTokenTtl.isNegative())) {
            throw new IllegalArgumentException("security.jwt.refresh-token-ttl must be positive");
        }
    }
}
