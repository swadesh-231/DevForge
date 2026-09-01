package com.devforge.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.cookie")
public record CookieProperties(
        @NotBlank
        @DefaultValue("devforge_refresh_token")
        String refreshTokenName,

        @DefaultValue("true")
        boolean secure,

        @NotBlank
        @Pattern(regexp = "Strict|Lax|None")
        @DefaultValue("Lax")
        String sameSite,

        @NotBlank
        @DefaultValue("/api/auth")
        String path,

        String domain

) {
}
