package com.devforge.security.cookie;

import com.devforge.config.CookieProperties;
import com.devforge.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieFactory {
    private final CookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public ResponseCookie create(String token) {
        return baseBuilder(token, jwtProperties.refreshToken().expiration()).build();
    }

    public ResponseCookie expire() {
        return baseBuilder("", Duration.ZERO).build();
    }

    public String cookieName() {
        return cookieProperties.refreshTokenName();
    }

    public String read(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, cookieProperties.refreshTokenName());
        return cookie == null ? null : cookie.getValue();
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
