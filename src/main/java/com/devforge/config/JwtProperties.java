package com.devforge.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(

        @NotNull
        @Valid
        TokenProperties accessToken,

        @NotNull
        @Valid
        TokenProperties refreshToken

) {
    public record TokenProperties(

            @NotBlank
            @Size(min = 32, message = "JWT secret must be at least 32 characters for HS256")
            String secretKey,

            @NotNull
            @DurationUnit(ChronoUnit.MILLIS)
            Duration expiration

    ) {
        public TokenProperties {
            if (expiration != null && (expiration.isZero() || expiration.isNegative())) {
                throw new IllegalArgumentException("JWT expiration must be positive");
            }
        }
    }
}
