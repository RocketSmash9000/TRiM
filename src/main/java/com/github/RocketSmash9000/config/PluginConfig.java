package com.github.RocketSmash9000.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.RocketSmash9000.util.AppDirectories;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Manages plugin configuration including enabled/disabled state.
 */
public class PluginConfig {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    
    private final File configFile;
    private Map<String, PluginState> pluginStates;
    
    public PluginConfig() {
        this.configFile = AppDirectories.getConfigFile().toFile();
        this.pluginStates = new HashMap<>();
        load();
    }
    
    /**
     * Loads the plugin configuration from the config file.
     * Creates a new config file with default values if it doesn't exist.
     */
    private void load() {
        try {
            if (configFile.exists()) {
                String content = new String(Files.readAllBytes(configFile.toPath()));
                if (!content.trim().isEmpty()) {
                    pluginStates = objectMapper.readValue(content, new TypeReference<Map<String, PluginState>>() {});
                }
            } else {
                // Create default config if it doesn't exist
                save();
            }
        } catch (IOException e) {
            System.err.println("Failed to load plugin config: " + e.getMessage());
            pluginStates = new HashMap<>();
        }
    }
    
    /**
     * Saves the current plugin configuration to the config file.
     */
    public synchronized void save() {
        try {
            objectMapper.writeValue(configFile, pluginStates);
        } catch (IOException e) {
            System.err.println("Failed to save plugin config: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a plugin is enabled.
     * 
     * @param pluginId The ID of the plugin to check
     * @return true if the plugin is enabled, false if disabled or not found
     */
    public boolean isPluginEnabled(String pluginId) {
        PluginState state = pluginStates.get(pluginId);
        return state != null && state.isEnabled();
    }
    
    /**
     * Sets whether a plugin is enabled.
     * 
     * @param pluginId The ID of the plugin to update
     * @param enabled Whether the plugin should be enabled
     */
    public void setPluginEnabled(String pluginId, boolean enabled) {
        pluginStates.computeIfAbsent(pluginId, id -> new PluginState()).setEnabled(enabled);
        save();
    }
    
    /**
     * Updates the plugin state with information from a loaded plugin.
     * 
     * @param pluginId The ID of the plugin
     * @param version The version of the plugin
     */
    public void updatePluginInfo(String pluginId, String version) {
        PluginState state = pluginStates.computeIfAbsent(pluginId, id -> new PluginState());
        state.setVersion(version);
        save();
    }
    
    /**
     * Gets all plugin states.
     * 
     * @return An unmodifiable map of plugin states
     */
    public Map<String, PluginState> getPluginStates() {
        return Collections.unmodifiableMap(pluginStates);
    }
    
    /**
     * Represents the state of a plugin in the configuration.
     */
    public static class PluginState {
        private boolean enabled = true; // By default, plugins are enabled
        private String version;
        private Map<String, Object> settings = new HashMap<>();
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getVersion() {
            return version;
        }
        
        public void setVersion(String version) {
            this.version = version;
        }
        
        public Map<String, Object> getSettings() {
            return settings;
        }
        
        public void setSettings(Map<String, Object> settings) {
            this.settings = settings != null ? settings : new HashMap<>();
        }
        
        public <T> T getSetting(String key, Class<T> type) {
            return type.cast(settings.get(key));
        }
        
        public void setSetting(String key, Object value) {
            settings.put(key, value);
        }
    }
}
