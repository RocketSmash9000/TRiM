package com.github.RocketSmash9000.ui.dialogs;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.Optional;

/**
 * Utility class for showing dialogs.
 */
public class DialogUtils {

    /**
     * Shows a custom dialog with the specified content and title.
     *
     * @param title   The dialog title
     * @param content The dialog content
     * @param owner   The owner window (can be null)
     */
    public static void showDialog(String title, Node content, Window owner) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);
        
        if (owner != null) {
            dialog.initOwner(owner);
        }

        // Set the dialog icon once the window is shown (scene is available)
        dialog.setOnShown(ev -> {
            try {
                Window w = dialog.getDialogPane().getScene().getWindow();
                if (w instanceof Stage s) {
                    URL iconUrl = DialogUtils.class.getResource("/images/app-icon.png");
                    if (iconUrl != null) {
                        s.getIcons().add(new Image(iconUrl.toExternalForm()));
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load application icon: " + e.getMessage());
            }
        });

        DialogPane dialogPane = dialog.getDialogPane();
        if (content instanceof DialogPane providedPane) {
            // If FXML root is a DialogPane, use it directly
            dialog.setDialogPane(providedPane);
            if (providedPane.getButtonTypes().isEmpty()) {
                providedPane.getButtonTypes().add(ButtonType.CLOSE);
            }
        } else {
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        }
        
        // Set minimum size
        dialogPane.setMinWidth(500);
        dialogPane.setMinHeight(400);

        dialog.showAndWait();
    }

    /**
     * Shows an information dialog.
     */
    public static void showInfoDialog(String title, String message, Window owner) {
        showAlert(Alert.AlertType.INFORMATION, title, message, owner);
    }

    /**
     * Shows a warning dialog.
     */
    public static void showWarningDialog(String title, String message, Window owner) {
        showAlert(Alert.AlertType.WARNING, title, message, owner);
    }

    /**
     * Shows an error dialog.
     */
    public static void showErrorDialog(String title, String message, Window owner) {
        showAlert(Alert.AlertType.ERROR, title, message, owner);
    }

    /**
     * Shows a confirmation dialog.
     *
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmDialog(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        if (owner != null) {
            alert.initOwner(owner);
        }

        // Set the dialog icon once the window is shown (scene is available)
        alert.setOnShown(ev -> {
            try {
                Window w = alert.getDialogPane().getScene().getWindow();
                if (w instanceof Stage s) {
                    URL iconUrl = DialogUtils.class.getResource("/images/app-icon.png");
                    if (iconUrl != null) {
                        s.getIcons().add(new Image(iconUrl.toExternalForm()));
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load application icon: " + e.getMessage());
            }
        });

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void showAlert(Alert.AlertType type, String title, String message, Window owner) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        if (owner != null) {
            alert.initOwner(owner);
        }

        // Set the dialog icon once the window is shown (scene is available)
        alert.setOnShown(ev -> {
            try {
                Window w = alert.getDialogPane().getScene().getWindow();
                if (w instanceof Stage s) {
                    java.net.URL iconUrl = DialogUtils.class.getResource("/images/app-icon.png");
                    if (iconUrl != null) {
                        s.getIcons().add(new Image(iconUrl.toExternalForm()));
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load application icon: " + e.getMessage());
            }
        });

        alert.showAndWait();
    }
}
