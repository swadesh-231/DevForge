package com.devforge.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "security.cors")
public record CorsProperties(

        @NotEmpty
        @DefaultValue({"http://localhost:5173", "http://localhost:5174"})
        List<String> allowedOrigins

) {
}
