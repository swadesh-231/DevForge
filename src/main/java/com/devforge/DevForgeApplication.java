package com.devforge;

import com.devforge.config.EnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DevForgeApplication {
    public static void main(String[] args) {
        EnvConfig.load();
        SpringApplication.run(DevForgeApplication.class, args);
    }

}
