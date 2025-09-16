package com.github.RocketSmash9000.plugin;

import com.github.RocketSmash9000.config.PluginConfig;
import org.pf4j.*;

import java.nio.file.Path;
import java.util.*;

/**
 * Manages the lifecycle of TRiM plugins.
 * Handles loading, unloading, and accessing plugins.
 */
public class PluginManager {
    private final org.pf4j.PluginManager pluginManager;
    private final Map<String, TRiMPlugin> loadedPlugins = new HashMap<>();
    private final Path pluginsDir;
    private boolean initialized = false;
    private final boolean developmentMode;
    private final PluginConfig pluginConfig;

    /**
     * Creates a new PluginManager with the specified configuration.
     * 
     * @param pluginsDir The directory where plugins are stored
     * @param developmentMode Whether to run in development mode (loads plugins from build directories)
     */
    public PluginManager(Path pluginsDir, boolean developmentMode) {
        this.pluginsDir = pluginsDir;
        this.developmentMode = developmentMode;
        this.pluginConfig = new PluginConfig();
        
        this.pluginManager = new DefaultPluginManager(pluginsDir) {
            @Override
            protected ExtensionFactory createExtensionFactory() {
                return new DefaultExtensionFactory();
            }
            
            @Override
            protected PluginFactory createPluginFactory() {
                return new TRiMPluginFactory();
            }
            
            @Override
            public boolean isDevelopment() {
                return developmentMode;
            }
        };
    }

    /**
     * Initializes the plugin manager and loads all available plugins.
     */
	@SuppressWarnings("D")
    public void initialize() {
        if (initialized) {
            return;
        }

        // Ensure plugins directory exists
        if (!pluginsDir.toFile().exists()) {
            if (!pluginsDir.toFile().mkdirs()) {
                throw new RuntimeException("Failed to create plugins directory: " + pluginsDir);
            }
        }

        // Load plugins
        pluginManager.loadPlugins();

        // Start only plugins that are enabled in the configuration
        for (PluginWrapper wrapper : pluginManager.getPlugins()) {
            String pid = wrapper.getPluginId();
            boolean enabled = pluginConfig.isPluginEnabled(pid);
            if (enabled) {
                pluginManager.startPlugin(pid);
            } else if (wrapper.getPluginState() == PluginState.STARTED) {
                pluginManager.stopPlugin(pid);
            }
        }

        // Get all started plugins
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        
        // Initialize TRiM plugins
        for (PluginWrapper pluginWrapper : startedPlugins) {
            TRiMPlugin plugin = getTRiMPlugin(pluginWrapper);
            if (plugin != null && isPluginEnabled(plugin)) {
                try {
                    // Update plugin info in config
                    pluginConfig.updatePluginInfo(plugin.getPluginId(), plugin.getVersion());
                    
                    // Initialize the plugin
                    plugin.onLoad();
                    loadedPlugins.put(plugin.getPluginId(), plugin);
                    System.out.println("Loaded plugin: " + plugin.getDisplayName() + " v" + plugin.getVersion());
                } catch (Exception e) {
                    System.err.println("Failed to initialize plugin " + pluginWrapper.getPluginId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        initialized = true;
    }

    /**
     * Shuts down the plugin manager and unloads all plugins.
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }
        
        // Unload all TRiM plugins
        for (TRiMPlugin plugin : new ArrayList<>(loadedPlugins.values())) {
            try {
                plugin.onUnload();
            } catch (Exception e) {
                System.err.println("Error unloading plugin " + plugin.getPluginId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Clear loaded plugins
        loadedPlugins.clear();
        
        // Stop and unload all plugins
        pluginManager.stopPlugins();
        pluginManager.unloadPlugins();
        
        initialized = false;
    }
    
    /**
     * Gets a loaded plugin by its ID.
     * 
     * @param pluginId The ID of the plugin to get
     * @return The plugin, or null if not found
     */
    public TRiMPlugin getPlugin(String pluginId) {
        return loadedPlugins.get(pluginId);
    }
    
    /**
     * Gets all loaded plugins.
     * 
     * @return A collection of all loaded plugins
     */
    public Collection<TRiMPlugin> getPlugins() {
        return Collections.unmodifiableCollection(loadedPlugins.values());
    }
    
    /**
     * Gets all extensions of the specified type from all plugins.
     * 
     * @param type The extension type class
     * @param <T> The extension type
     * @return A list of extensions
     */
    public <T> List<T> getExtensions(Class<T> type) {
        return pluginManager.getExtensions(type);
    }
    
    /**
     * Gets the PF4J plugin manager instance.
     * 
     * @return The PF4J plugin manager
     */
    public org.pf4j.PluginManager getPf4jPluginManager() {
        return pluginManager;
    }
    
    // Helper method to get a TRiM plugin from a plugin wrapper
    private TRiMPlugin getTRiMPlugin(PluginWrapper pluginWrapper) {
        try {
            Object plugin = pluginWrapper.getPlugin();
            if (plugin instanceof TRiMPlugin) {
                return (TRiMPlugin) plugin;
            }
        } catch (Exception e) {
            System.err.println("Error accessing plugin " + pluginWrapper.getPluginId() + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Checks if a plugin is enabled in the configuration.
     * 
     * @param plugin The plugin to check
     * @return true if the plugin is enabled, false otherwise
     */
    public boolean isPluginEnabled(TRiMPlugin plugin) {
        return pluginConfig.isPluginEnabled(plugin.getPluginId());
    }

    /**
     * Checks if a plugin is enabled in the configuration by its ID.
     *
     * @param pluginId the plugin id
     * @return true if enabled in config, false otherwise
     */
    public boolean isPluginEnabled(String pluginId) {
        return pluginConfig.isPluginEnabled(pluginId);
    }
    
    /**
     * Enables or disables a plugin.
     * 
     * @param pluginId The ID of the plugin to enable/disable
     * @param enabled Whether the plugin should be enabled
     */
	@SuppressWarnings("D")
    public void setPluginEnabled(String pluginId, boolean enabled) {
        pluginConfig.setPluginEnabled(pluginId, enabled);
        
        PluginWrapper wrapper = pluginManager.getPlugin(pluginId);
        if (wrapper == null) {
            // Plugin not found in PF4J registry (maybe removed). Nothing else to do.
            return;
        }

        if (enabled) {
            // Start PF4J plugin if not started
            if (wrapper.getPluginState() != PluginState.STARTED) {
                pluginManager.startPlugin(pluginId);
                wrapper = pluginManager.getPlugin(pluginId); // refresh state
            }
            // Initialize TRiM plugin if not already loaded
            if (!loadedPlugins.containsKey(pluginId)) {
                TRiMPlugin plugin = getTRiMPlugin(wrapper);
                if (plugin != null) {
                    try {
                        pluginConfig.updatePluginInfo(plugin.getPluginId(), plugin.getVersion());
                        plugin.onLoad();
                        loadedPlugins.put(plugin.getPluginId(), plugin);
                    } catch (Exception e) {
                        System.err.println("Error initializing plugin " + pluginId + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            // If currently loaded at TRiM level, unload it first
            if (loadedPlugins.containsKey(pluginId)) {
                TRiMPlugin plugin = loadedPlugins.remove(pluginId);
                try {
                    plugin.onUnload();
                } catch (Exception e) {
                    System.err.println("Error unloading plugin " + pluginId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            // Stop PF4J plugin if started
            if (wrapper.getPluginState() == PluginState.STARTED) {
                pluginManager.stopPlugin(pluginId);
            }
        }
    }
    
    /**
     * Custom plugin factory for TRiM plugins.
     */
    private static class TRiMPluginFactory extends DefaultPluginFactory {
        @Override
        public Plugin create(PluginWrapper pluginWrapper) {
            try {
                // Let PF4J create the plugin instance
                Plugin plugin = super.create(pluginWrapper);
                
                // Verify it's a TRiM plugin
                if (!(plugin instanceof TRiMPlugin)) {
                    throw new RuntimeException("Plugin " + pluginWrapper.getPluginId() + 
                                              " does not implement TRiMPlugin interface");
                }
                
                return plugin;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create plugin " + pluginWrapper.getPluginId(), e);
            }
        }
    }
}
