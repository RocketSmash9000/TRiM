package com.github.RocketSmash9000.plugin.example;

import com.github.RocketSmash9000.plugin.AbstractTRiMPlugin;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

/**
 * An example plugin that demonstrates the TRiM plugin system.
 */
@Extension
public class ExamplePlugin extends AbstractTRiMPlugin implements ExtensionPoint {
    
    public ExamplePlugin() {
        super(
            "com.github.RocketSmash9000.example",  // Plugin ID
            "Example Plugin",                      // Display name
            "1.0.0",                               // Plugin version
            "1.0.0"                                // Minimum TRiM version required
        );
    }
    
    @Override
    public void onLoad() {
        System.out.println("ExamplePlugin loaded!");
        // Initialize your plugin here
    }
    
    @Override
    public void onUnload() {
        System.out.println("ExamplePlugin unloaded!");
        // Clean up resources here
    }
    
    /**
     * Example method specific to this plugin.
     */
    public String sayHello(String name) {
        return "Hello, " + name + "! This is the ExamplePlugin.";
    }
}
