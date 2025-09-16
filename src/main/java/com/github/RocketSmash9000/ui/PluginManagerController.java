package com.github.RocketSmash9000.ui;

import com.github.RocketSmash9000.plugin.PluginManager;
import com.github.RocketSmash9000.plugin.TRiMPlugin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.Comparator;
import java.util.Objects;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

/**
 * Controller for the Plugin Manager dialog.
 */
public class PluginManagerController {

    @FXML
    private ListView<PluginItem> pluginList;
    @FXML
    private Label descriptionLabel;

    private final PluginManager pluginManager;
    private final ObservableList<PluginItem> plugins = FXCollections.observableArrayList();

    public PluginManagerController(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @FXML
    public void initialize() {
        setupPluginList();
        loadPlugins();
    }

    private void setupPluginList() {
        // Custom cell factory for the plugin list
        pluginList.setCellFactory(listView -> new PluginListCell());
        pluginList.setItems(plugins);

        // Update description when selection changes
        pluginList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                descriptionLabel.setText(newVal.getDescription());
            } else {
                descriptionLabel.setText("");
            }
        });
    }

	@SuppressWarnings("D")
    private void loadPlugins() {
        plugins.clear();
        
        // Build list from PF4J plugin descriptors so disabled plugins also appear
        for (PluginWrapper wrapper : pluginManager.getPf4jPluginManager().getPlugins()) {
            PluginDescriptor d = wrapper.getDescriptor();
            String id = d.getPluginId();
            String version = d.getVersion();
            boolean enabled = pluginManager.isPluginEnabled(id);

            // Prefer TRiMPlugin metadata if plugin is started; else fall back to descriptor/id
            String name = id;
            String description = d.getPluginDescription() != null ? d.getPluginDescription() : "No description available.";

            if (wrapper.getPluginState() == PluginState.STARTED) {
                try {
                    Object obj = wrapper.getPlugin();
                    if (obj instanceof TRiMPlugin trim) {
                        name = trim.getDisplayName();
                        description = trim.getDescription() != null ? trim.getDescription() : description;
                    }
                } catch (Exception ignored) {
                    // ignore and use descriptor fallback
                }
            }

            plugins.add(new PluginItem(id, name, version, enabled, description));
        }

        plugins.sort(Comparator.comparing(PluginItem::getName, String.CASE_INSENSITIVE_ORDER));
    }

    /**
     * Toggles the enabled state of a plugin.
     */
    public void togglePlugin(PluginItem pluginItem) {
        pluginManager.setPluginEnabled(pluginItem.getId(), pluginItem.isEnabled());
        // TODO: Add visual feedback or confirmation
    }

    /**
     * Represents a plugin in the list view.
     */
    public static class PluginItem {
        private final String id;
        private final String name;
        private final String version;
        private boolean enabled;
        private final String description;

        public PluginItem(String id, String name, String version, boolean enabled, String description) {
            this.id = id;
            this.name = name;
            this.version = version;
            this.enabled = enabled;
            this.description = description != null ? description : "No description available.";
        }

        // Getters and setters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getVersion() { return version; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getDescription() { return description; }

        @Override
        public String toString() {
            return String.format("%s v%s", name, version);
        }
    }

    /**
     * Custom list cell for plugin items with a checkbox.
     */
    private class PluginListCell extends ListCell<PluginItem> {
        private final CheckBox checkBox = new CheckBox();
        private final Label nameLabel = new Label();
        private final Label versionLabel = new Label();
        private final VBox content = new VBox(2);

        public PluginListCell() {
            super();
            
            // Setup layout
            HBox hbox = new HBox(10);
            hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            nameLabel.setStyle("-fx-font-weight: bold;");
            versionLabel.setStyle("-fx-font-size: 0.9em; -fx-text-fill: -fx-text-base-color; -fx-opacity: 0.8;");
            
            VBox labels = new VBox(2, nameLabel, versionLabel);
            hbox.getChildren().addAll(checkBox, labels);
            
            content.getChildren().add(hbox);
            content.setPadding(new javafx.geometry.Insets(5));
            
            // Handle checkbox changes
            checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (getItem() != null) {
                    getItem().setEnabled(isSelected);
                    togglePlugin(getItem());
                }
            });
        }

        @Override
        protected void updateItem(PluginItem item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                nameLabel.setText(item.getName());
                versionLabel.setText(String.format("v%s", item.getVersion()));
                checkBox.setSelected(item.isEnabled());
                setGraphic(content);
                setText(null);
                
                // Add style class for theming
                getStyleClass().setAll("plugin-item");
            }
        }
    }
}
