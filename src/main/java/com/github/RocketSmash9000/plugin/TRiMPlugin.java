package com.github.RocketSmash9000.plugin;

/**
 * Base interface for all TRiM plugins.
 * All plugins must implement this interface to be recognized by the TRiM application.
 */
public interface TRiMPlugin {
    
    /**
     * Called when the plugin is first loaded.
     * Perform any initialization here.
     */
    void onLoad();
    
    /**
     * Called when the plugin is being unloaded.
     * Perform any cleanup here.
     */
    void onUnload();
    
    /**
     * @return The unique identifier for this plugin.
     */
    String getPluginId();
    
    /**
     * @return The display name of the plugin.
     */
    String getDisplayName();
    
    /**
     * @return The version of the plugin.
     */
    String getVersion();
    
    /**
     * @return The minimum required version of the TRiM application this plugin is compatible with.
     */
    String getMinimumApplicationVersion();
    
    /**
     * @return A description of what this plugin does.
     */
    default String getDescription() {
        return "No description available.";
    }
}
