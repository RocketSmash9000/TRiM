package com.github.RocketSmash9000.plugin;

/**
 * Abstract base class for TRiM plugins that provides default implementations
 * for common plugin functionality.
 */
public abstract class AbstractTRiMPlugin implements TRiMPlugin {
    
    private final String pluginId;
    private final String displayName;
    private final String version;
    private final String minimumApplicationVersion;
    
    /**
     * Creates a new plugin with the specified metadata.
     * 
     * @param pluginId The unique identifier for this plugin (should follow reverse domain name notation, e.g., "com.example.myplugin")
     * @param displayName The display name of the plugin
     * @param version The version of the plugin (should follow semantic versioning)
     * @param minimumApplicationVersion The minimum required version of the TRiM application this plugin is compatible with
     */
    protected AbstractTRiMPlugin(String pluginId, String displayName, String version, String minimumApplicationVersion) {
        if (pluginId == null || pluginId.trim().isEmpty()) {
            throw new IllegalArgumentException("Plugin ID cannot be null or empty");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be null or empty");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version cannot be null or empty");
        }
        if (minimumApplicationVersion == null || minimumApplicationVersion.trim().isEmpty()) {
            throw new IllegalArgumentException("Minimum application version cannot be null or empty");
        }
        
        this.pluginId = pluginId;
        this.displayName = displayName;
        this.version = version;
        this.minimumApplicationVersion = minimumApplicationVersion;
    }
    
    @Override
    public void onLoad() {
        // Default implementation does nothing
    }
    
    @Override
    public void onUnload() {
        // Default implementation does nothing
    }
    
    @Override
    public String getPluginId() {
        return pluginId;
    }
    
    @Override
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String getVersion() {
        return version;
    }
    
    @Override
    public String getMinimumApplicationVersion() {
        return minimumApplicationVersion;
    }
    
    @Override
    public String toString() {
        return String.format("%s (ID: %s, Version: %s)", displayName, pluginId, version);
    }
}
