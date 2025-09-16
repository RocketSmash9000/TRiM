package com.github.RocketSmash9000.plugin;

import com.github.RocketSmash9000.util.AppDirectories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PluginManagerTest {
    private PluginManager pluginManager;
    private Path testPluginsDir;

    @BeforeEach
    void setUp() {
        // Use a temporary directory for testing
        testPluginsDir = AppDirectories.getAppDir().resolve("test-plugins");
        testPluginsDir.toFile().mkdirs();
        
        // Create plugin manager in development mode for testing
        pluginManager = new PluginManager(testPluginsDir, true);
    }

    @AfterEach
    void tearDown() {
        if (pluginManager != null) {
            pluginManager.shutdown();
        }
        // Clean up test directory
        deleteDirectory(testPluginsDir.toFile());
    }

    @Test
    void testPluginLifecycle() {
        // Initialize the plugin manager
        pluginManager.initialize();
        
        // Verify no plugins are loaded in the test environment
        // (since we haven't added any test plugins yet)
        assertTrue(pluginManager.getPlugins().isEmpty());
        
        // Shutdown should complete without errors
        pluginManager.shutdown();
    }

    @Test
    void testGetNonExistentPlugin() {
        pluginManager.initialize();
        assertNull(pluginManager.getPlugin("non.existent.plugin"));
    }

    // Helper method to delete test directories
    private void deleteDirectory(java.io.File directory) {
        if (directory.exists()) {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}
