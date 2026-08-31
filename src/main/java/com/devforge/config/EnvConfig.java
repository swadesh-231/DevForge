package com.devforge.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class EnvConfig {
    private EnvConfig() {
    }
    public static void load() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry -> {
            if (System.getenv(entry.getKey()) == null
                    && System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }
}