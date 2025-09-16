package com.github.RocketSmash9000.plugin.ui;

import javafx.scene.Node;

/**
 * UI extension point: allows plugins to contribute buttons to TRiM's toolbar.
 *
 * Implementations should be annotated with {@code @org.pf4j.Extension}
 * and reside in the plugin JAR.
 */
public interface ToolbarButtonExtension {
    /**
     * Text to show on the button. May be empty if a graphic is provided.
     */
    String getText();

    /**
     * Optional tooltip to show when hovering the button.
     */
    default String getTooltip() { return null; }

    /**
     * Optional graphic node (e.g., icon) for the button.
     */
    default Node getGraphic() { return null; }

    /**
     * Optional inline style string for the button (e.g., -fx-font-size: 14px;).
     */
    default String getStyle() { return null; }

    /**
     * Whether the button should start disabled.
     */
    default boolean initiallyDisabled() { return false; }

    /**
     * Invoked when the button is clicked.
     * Note: This will be called on the JavaFX Application Thread.
     */
    void onAction();
}
