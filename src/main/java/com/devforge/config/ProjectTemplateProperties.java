package com.devforge.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.project-template")
public record ProjectTemplateProperties(

        @NotBlank
        @DefaultValue("react-starter")
        String name

) {
}
