package com.devforge.security.cookie;

import com.devforge.config.CookieProperties;
import com.devforge.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieFactory {
    private final CookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public ResponseCookie create(String token) {
        return baseBuilder(token, jwtProperties.refreshTokenTtl()).build();
    }

    public ResponseCookie expire() {
        return baseBuilder("", Duration.ZERO).build();
    }

    public String cookieName() {
        return cookieProperties.refreshTokenName();
    }

    private ResponseCookie.ResponseCookieBuilder baseBuilder(String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
                .from(cookieProperties.refreshTokenName(), value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(cookieProperties.path())
                .maxAge(maxAge);

        if (cookieProperties.domain() != null && !cookieProperties.domain().isBlank()) {
            builder.domain(cookieProperties.domain());
        }
        return builder;
    }
}
