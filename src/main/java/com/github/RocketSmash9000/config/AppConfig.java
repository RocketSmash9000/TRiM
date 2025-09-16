package com.github.RocketSmash9000.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.RocketSmash9000.util.AppDirectories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages application configuration including volume settings.
 */
public class AppConfig {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    
    private static final String CONFIG_FILENAME = "app_config.json";
    private static final double DEFAULT_VOLUME = 0.1; // 10% volume by default
    
    private final Path configPath;
    private double volume;
    
    public AppConfig() {
        this.configPath = AppDirectories.getAppDir().resolve(CONFIG_FILENAME);
        this.volume = DEFAULT_VOLUME;
        load();
    }
    
    /**
     * Loads the application configuration from the config file.
     * Creates a new config file with default values if it doesn't exist.
     */
    private void load() {
        try {
            if (Files.exists(configPath)) {
                String content = Files.readString(configPath);
                if (!content.trim().isEmpty()) {
                    AppConfigData data = objectMapper.readValue(content, AppConfigData.class);
                    this.volume = data.getVolume();
                } else {
                    // empty file, save defaults
                    save();
                }
            } else {
                // Save default config if it doesn't exist
                save();
            }
        } catch (IOException e) {
            System.err.println("Failed to load application config: " + e.getMessage());
        } catch (Exception e) {
            // Any JSON parse / mapping error, fallback to defaults and rewrite file
            System.err.println("Failed to parse application config, using defaults: " + e.getMessage());
            this.volume = DEFAULT_VOLUME;
            save();
        }
    }
    
    /**
     * Saves the current configuration to the config file.
     */
    private void save() {
        try {
            AppConfigData data = new AppConfigData(volume);
            String json = objectMapper.writeValueAsString(data);
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, json);
        } catch (IOException e) {
            System.err.println("Failed to save application config: " + e.getMessage());
        }
    }
    
    public double getVolume() {
        return volume;
    }
    
    public void setVolume(double volume) {
        // Ensure volume is within valid range [0.0, 1.0]
        this.volume = Math.max(0.0, Math.min(1.0, volume));
        save();
    }
    
    /**
     * Data class for JSON serialization/deserialization.
     */
    public static class AppConfigData {
        private double volume;

        // No-args constructor for Jackson
        public AppConfigData() {}

        public AppConfigData(double volume) {
            this.volume = volume;
        }

        public double getVolume() {
            return volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }
    }
}
