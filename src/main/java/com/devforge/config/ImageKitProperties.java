package com.devforge.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "app.imagekit")
public record ImageKitProperties(

        @NotBlank
        String publicKey,

        @NotBlank
        String privateKey,

        @NotBlank
        String urlEndpoint,

        @NotBlank
        @DefaultValue("/devforge/avatars")
        String avatarFolder,

        @Positive
        @DefaultValue("2097152")
        long maxAvatarBytes,

        @NotEmpty
        @DefaultValue({"image/jpeg", "image/png", "image/webp"})
        Set<String> allowedContentTypes

) {
}
