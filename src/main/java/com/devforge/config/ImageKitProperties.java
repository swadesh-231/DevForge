package com.devforge.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "imagekit")
public record ImageKitProperties(

        @NotBlank
        String privateKey,

        @NotBlank
        String urlEndpoint,

        @NotBlank
        String avatarFolder,

        @Positive
        long maxAvatarBytes,

        Set<String> allowedContentTypes

) {
}
