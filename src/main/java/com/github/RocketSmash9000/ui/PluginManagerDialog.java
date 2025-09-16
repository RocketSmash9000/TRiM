package com.github.RocketSmash9000.ui;

import com.github.RocketSmash9000.plugin.PluginManager;
import com.github.RocketSmash9000.ui.dialogs.DialogUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

/**
 * A dialog for managing plugins.
 */
public class PluginManagerDialog {

    private final PluginManager pluginManager;
    private final Window owner;

    /**
     * Creates a new PluginManagerDialog.
     *
     * @param pluginManager The plugin manager instance
     * @param owner         The owner window (can be null)
     */
    public PluginManagerDialog(PluginManager pluginManager, Window owner) {
        this.pluginManager = pluginManager;
        this.owner = owner;
    }

    /**
     * Shows the plugin manager dialog.
     */
    public void show() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PluginManagerDialog.fxml"));
            
            // Explicitly set controller because FXML does not declare fx:controller
            loader.setController(new PluginManagerController(pluginManager));
            
            // Load the root node
            Parent root = loader.load();
            
            // Show the dialog
            DialogUtils.showDialog("Plugin Manager", root, owner);
            
        } catch (IOException e) {
            System.err.println("Failed to load plugin manager dialog: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to a simple alert if FXML loading fails
            DialogUtils.showErrorDialog("Error", 
                "Failed to load the plugin manager. Please check the logs for details.", 
                owner);
        }
    }
    
    /**
     * Shows the plugin manager dialog with the specified owner window.
     *
     * @param pluginManager The plugin manager instance
     * @param owner         The owner window (can be null)
     */
    public static void show(PluginManager pluginManager, Window owner) {
        new PluginManagerDialog(pluginManager, owner).show();
    }
}
