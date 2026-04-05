package com.moodify.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EnvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> envMap = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                envMap.put(entry.getKey(), entry.getValue());
                System.setProperty(entry.getKey(), entry.getValue());
            });

            environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envMap));

            System.out.println("=== .env FILE LOADED ===");
            System.out.println("SPOTIFY_CLIENT_ID: " + (dotenv.get("SPOTIFY_CLIENT_ID") != null ? "SET" : "MISSING"));
            System.out.println("SPOTIFY_CLIENT_SECRET: " + (dotenv.get("SPOTIFY_CLIENT_SECRET") != null ? "SET" : "MISSING"));

        } catch (Exception e) {
            System.err.println("WARNING: Could not load .env file: " + e.getMessage());
        }
    }
}