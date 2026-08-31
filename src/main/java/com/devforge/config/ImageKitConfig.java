package com.devforge.config;

import io.imagekit.client.ImageKitClient;
import io.imagekit.client.okhttp.ImageKitOkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        ImageKitProperties.class,
        JwtProperties.class,
        CookieProperties.class,
        CorsProperties.class
})
public class ImageKitConfig {
    @Bean
    public ImageKitClient imageKitClient(ImageKitProperties properties) {
        return ImageKitOkHttpClient.builder()
                .privateKey(properties.privateKey())
                .build();
    }
}
