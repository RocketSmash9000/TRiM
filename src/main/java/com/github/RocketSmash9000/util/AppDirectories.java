package com.github.RocketSmash9000.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for managing application directories and files.
 */
public class AppDirectories {
    private static final String APP_NAME = "TRiM";
    private static final Path APP_DIR;
    private static final Path PLUGINS_DIR;
    private static final Path STYLES_DIR;
    private static final Path CONFIG_FILE;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        Path baseDir;

        if (os.contains("win")) {
            // Windows: %APPDATA%\TRiM
            baseDir = Paths.get(System.getenv("APPDATA"), APP_NAME);
        } else {
            // Linux/Unix: ~/.config/TRiM
            baseDir = Paths.get(System.getProperty("user.home"), ".config", APP_NAME);
        }

        APP_DIR = baseDir;
        PLUGINS_DIR = baseDir.resolve("plugins");
        STYLES_DIR = baseDir.resolve("styles");
        CONFIG_FILE = baseDir.resolve("config.json");

        // Create directories if they don't exist
        try {
            Files.createDirectories(PLUGINS_DIR);
            Files.createDirectories(STYLES_DIR);
            
            // Create default config file if it doesn't exist
            if (!Files.exists(CONFIG_FILE)) {
                Files.createFile(CONFIG_FILE);
                // Initialize with empty JSON object
                Files.writeString(CONFIG_FILE, "{}\n");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize application directories", e);
        }
    }

    public static Path getAppDir() {
        return APP_DIR;
    }

    public static Path getPluginsDir() {
        return PLUGINS_DIR;
    }

    public static Path getStylesDir() {
        return STYLES_DIR;
    }

    public static Path getConfigFile() {
        return CONFIG_FILE;
    }
}
