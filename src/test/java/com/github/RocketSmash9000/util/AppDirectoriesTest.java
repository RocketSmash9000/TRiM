package com.github.RocketSmash9000.util;

import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppDirectoriesTest {

    @Test
    public void testDirectoriesExist() {
        // Test that all required directories exist
        assertTrue(Files.isDirectory(AppDirectories.getAppDir()));
        assertTrue(Files.isDirectory(AppDirectories.getPluginsDir()));
        assertTrue(Files.isDirectory(AppDirectories.getStylesDir()));
    }

    @Test
    public void testConfigFileExists() {
        // Test that the config file exists and is a regular file
        Path configFile = AppDirectories.getConfigFile();
        assertTrue(Files.exists(configFile));
        assertTrue(Files.isRegularFile(configFile));
    }

    @Test
    public void testDirectoryPaths() {
        // Test that directory paths are correctly constructed
        Path appDir = AppDirectories.getAppDir();
        assertTrue(appDir.endsWith("TRiM"));
        
        // Verify that the plugins directory is a subdirectory of the app directory
        Path pluginsDir = AppDirectories.getPluginsDir();
        assertEquals(appDir.resolve("plugins"), pluginsDir);
        
        // Verify config file is in the root app directory
        Path configFile = AppDirectories.getConfigFile();
        assertEquals(appDir.resolve("config.json"), configFile);
    }
}
