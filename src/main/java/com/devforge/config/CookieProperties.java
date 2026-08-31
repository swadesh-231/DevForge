package com.devforge.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.cookie")
public record CookieProperties(

        @NotBlank
        String refreshTokenName,

        boolean secure,

        @NotBlank
        @Pattern(regexp = "Strict|Lax|None")
        String sameSite,

        @NotBlank
        String path,

        String domain

) {
}
