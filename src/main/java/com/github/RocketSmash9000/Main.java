package com.github.RocketSmash9000;

import com.github.RocketSmash9000.audio.AudioFinder;
import com.github.RocketSmash9000.audio.AudioQueue;
import com.github.RocketSmash9000.audio.AudioMetadataExtractor;
import com.github.RocketSmash9000.config.AppConfig;
import com.github.RocketSmash9000.util.AppDirectories;
import com.github.RocketSmash9000.plugin.ui.ToolbarButtonExtension;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.binding.Bindings;
import javafx.scene.text.TextAlignment;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import com.github.RocketSmash9000.visualization.EQVisualizer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import javafx.scene.control.Alert;
import com.github.RocketSmash9000.plugin.PluginManager;
import com.github.RocketSmash9000.ui.PluginManagerDialog;

public class Main extends Application {
	private MediaPlayer mediaPlayer;
	private AudioQueue audioQueue;
	private Label nowPlayingLabel;
	private ProgressBar progressBar;
	private Button playPauseButton;
	private Button nextButton;
	private Button eqToggleButton;
	private Slider volumeSlider;
	private EQVisualizer eqVisualizer;
	private boolean isPlaying = false;
	private boolean eqEnabled = false;
	private PluginManager pluginManager;
	private AppConfig appConfig;

	@Override
	public void start(Stage primaryStage) {
		// Initialize configurations
		appConfig = new AppConfig();
		
		// Initialize plugin manager
		initializePluginManager();
		
		// Create UI elements
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(10));

		        // Top: Now Playing
        nowPlayingLabel = new Label("No track selected");
        nowPlayingLabel.setMaxWidth(Double.MAX_VALUE);
        nowPlayingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        nowPlayingLabel.setAlignment(Pos.CENTER);
        nowPlayingLabel.setTextAlignment(TextAlignment.CENTER);
        BorderPane.setMargin(nowPlayingLabel, new Insets(0, 0, 10, 0));

		// Create EQ Visualizer and Progress Bar container
		VBox visualizationContainer = new VBox();
		visualizationContainer.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(visualizationContainer, Priority.ALWAYS);
		
		// EQ Visualizer - make it responsive
		eqVisualizer = new EQVisualizer(mediaPlayer, 400, 60);
		eqVisualizer.setVisible(false);
		eqVisualizer.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(eqVisualizer, Priority.ALWAYS);
		
		// Bind width to container width minus some padding
		eqVisualizer.prefWidthProperty().bind(visualizationContainer.widthProperty().subtract(20));
		
		// Progress bar with centered container
		progressBar = new ProgressBar(0);
		progressBar.setPrefHeight(10);
		progressBar.setDisable(true);
		
		// Container to center the progress bar with max width
		HBox progressContainer = new HBox();
		progressContainer.setAlignment(Pos.CENTER);
		progressContainer.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(progressContainer, Priority.ALWAYS);
		
		// Bind progress bar width to 90% of its container's width
		progressBar.prefWidthProperty().bind(progressContainer.widthProperty().multiply(0.9));
		progressBar.maxWidthProperty().bind(progressContainer.widthProperty().multiply(0.9));
		
		progressContainer.getChildren().add(progressBar);
		
		visualizationContainer.getChildren().addAll(eqVisualizer, progressContainer);
		VBox.setVgrow(eqVisualizer, Priority.ALWAYS);
		VBox.setVgrow(progressContainer, Priority.NEVER);

		// Bottom: Controls
		HBox controls = new HBox(10);
		controls.setPadding(new Insets(10, 0, 0, 0));

		Button openButton = new Button("Open Folder");
		openButton.setOnAction(e -> openFolder(primaryStage));
		volumeSlider = new Slider(0.0, 1.0, appConfig.getVolume());
		volumeSlider.setPrefWidth(100);
		volumeSlider.setShowTickLabels(false);
		volumeSlider.setShowTickMarks(true);
		volumeSlider.setMajorTickUnit(0.25);
		volumeSlider.setBlockIncrement(0.1);
		volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
		    if (mediaPlayer != null) {
		        mediaPlayer.setVolume(newVal.doubleValue());
		    }
		    appConfig.setVolume(newVal.doubleValue());
		});

		playPauseButton = new Button("▶");
		playPauseButton.setOnAction(e -> togglePlayPause());
		playPauseButton.setDisable(true);

		nextButton = new Button("⏭");
		nextButton.setOnAction(e -> playNextTrack());
		nextButton.setDisable(true);

		eqToggleButton = new Button("EQ OFF");
		eqToggleButton.setOnAction(e -> {
            eqEnabled = !eqEnabled;
            eqToggleButton.setText(eqEnabled ? "EQ ON" : "EQ OFF");
            eqVisualizer.setEnabled(eqEnabled);
        });

        // Add plugin manager button
        Button pluginsButton = new Button("Plugins");
        pluginsButton.setOnAction(e -> {
            if (pluginManager != null) {
                new PluginManagerDialog(pluginManager, primaryStage).show();
            } else {
                showAlert("Plugin Error", "Plugin system failed to initialize. Please check the logs.");
            }

        });

        // Style buttons
        String buttonStyle = "-fx-font-size: 14px; -fx-min-width: 60px; -fx-min-height: 30px;";
        playPauseButton.setStyle(buttonStyle);
        nextButton.setStyle(buttonStyle);
        openButton.setStyle(buttonStyle);
        eqToggleButton.setStyle(buttonStyle);
        pluginsButton.setStyle(buttonStyle);

        controls.getChildren().addAll(openButton, playPauseButton, nextButton, eqToggleButton, pluginsButton);

        // Add plugin-provided toolbar buttons (if any)
        attachPluginToolbarButtons(controls);
        controls.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Create bottom controls with volume slider
        HBox bottomControls = new HBox(10);
        bottomControls.setAlignment(Pos.CENTER);
        bottomControls.setPadding(new Insets(10, 0, 0, 0));
        
        // Percentage label without decimals
        Label volumePercentLabel = new Label();
        volumePercentLabel.textProperty().bind(
            Bindings.createStringBinding(
                () -> (int)Math.round(volumeSlider.getValue() * 100) + "%",
                volumeSlider.valueProperty()
            )
        );
        
        // Add volume control to the right side
        HBox volumeBox = new HBox(5, volumeSlider, volumePercentLabel);
        volumeBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(volumeBox, Priority.ALWAYS);
        
        // Add all controls to the bottom
        bottomControls.getChildren().addAll(controls, volumeBox);

		// Add all to root
		VBox content = new VBox(5, nowPlayingLabel, visualizationContainer, bottomControls);
		VBox.setVgrow(visualizationContainer, Priority.ALWAYS);
		root.setCenter(content);
		root.setMinHeight(200);

		// Set up the scene
		Scene scene = new Scene(root, 550, 200);
		try {
			scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());
		} catch (Exception e) {
			System.err.println("Error loading dark theme: " + e.getMessage());
		}
		
		primaryStage.setTitle("TRiM Player");
		primaryStage.setScene(scene);
		primaryStage.show();

		// Event Handlers

		// Update progress bar
		new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (mediaPlayer != null) {
					double progress = mediaPlayer.getCurrentTime().toMillis() /
							mediaPlayer.getTotalDuration().toMillis();
					progressBar.setProgress(progress);
				}
			}
		}.start();
	}

	private void openFolder(Stage primaryStage) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select Music Folder");
		File selectedDirectory = directoryChooser.showDialog(primaryStage);

		if (selectedDirectory != null) {
			try {
				AudioFinder finder = new AudioFinder(selectedDirectory.getAbsolutePath());
				audioQueue = new AudioQueue(finder.getAudioFiles());
				playPauseButton.setDisable(false);
				nextButton.setDisable(false);
				playNextTrack();
			} catch (Exception e) {
				showError("Error loading music", e.getMessage());
			}
		}
	}

	private void togglePlayPause() {
		if (mediaPlayer != null) {
			if (isPlaying) {
				mediaPlayer.pause();
				playPauseButton.setText("▶");
			} else {
				mediaPlayer.play();
				playPauseButton.setText("⏸");
			}
			isPlaying = !isPlaying;
		}
	}

	private void playNextTrack() {
		if (audioQueue == null) return;

		// Stop current track if playing
		if (mediaPlayer != null) {
			try {
				mediaPlayer.stop();
				mediaPlayer.dispose();
			} catch (Exception e) {
				System.err.println("Error cleaning up media player: " + e.getMessage());
			} finally {
				mediaPlayer = null;
			}
		}

		File nextTrack = audioQueue.getNextTrack();
		if (nextTrack != null) {
			try {
				String mediaUrl = nextTrack.toURI().toString();
				Media media = new Media(mediaUrl);
				mediaPlayer = new MediaPlayer(media);
				mediaPlayer.setVolume(appConfig.getVolume());

				mediaPlayer.setOnReady(() -> {
					try {
						// Get track info from metadata
						String displayName = AudioMetadataExtractor.getDisplayName(nextTrack);
						nowPlayingLabel.setText(displayName);
						playPauseButton.setText("⏸");
						isPlaying = true;
						// Update EQ visualizer with the new media player
						eqVisualizer.setMediaPlayer(mediaPlayer);
						mediaPlayer.play();
					} catch (Exception e) {
						System.err.println("Error in media player ready handler: " + e.getMessage());
						playNextTrack(); // Skip to next track on error
					}
				});

				mediaPlayer.setOnEndOfMedia(this::playNextTrack);

				mediaPlayer.setOnError(() -> {
					showError("Playback Error", "Could not play: " + nextTrack.getName());
					playNextTrack(); // Skip to next track on error
				});

			} catch (Exception e) {
				showError("Error", "Could not play: " + nextTrack.getName());
				playNextTrack(); // Skip to next track on error
			}
		}
	}

	private void showError(String title, String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle(title);
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
		});
	}

	@Override
	public void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.dispose();
		}
	}

    /**
     * Discovers plugin-provided toolbar button extensions and adds them to the controls bar.
     */
    private void attachPluginToolbarButtons(HBox controls) {
        if (pluginManager == null) {
            return;
        }
        try {
            List<ToolbarButtonExtension> extensions = pluginManager.getExtensions(ToolbarButtonExtension.class);
            for (ToolbarButtonExtension ext : extensions) {
                try {
                    Button b = buildButton(ext);
                    controls.getChildren().add(b);
                } catch (Exception ex) {
                    System.err.println("Failed to build plugin toolbar button: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading toolbar button extensions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a JavaFX Button from a ToolbarButtonExtension.
     */
    private Button buildButton(ToolbarButtonExtension ext) {
        Button btn = new Button(ext.getText() != null ? ext.getText() : "");
        if (ext.getGraphic() != null) {
            btn.setGraphic(ext.getGraphic());
        }
        if (ext.getTooltip() != null && !ext.getTooltip().isBlank()) {
            btn.setTooltip(new Tooltip(ext.getTooltip()));
        }
        if (ext.getStyle() != null && !ext.getStyle().isBlank()) {
            btn.setStyle(ext.getStyle());
        }
        btn.setDisable(ext.initiallyDisabled());
        btn.setOnAction(e -> {
            try {
                ext.onAction();
            } catch (Exception ex) {
                // Ensure plugin exceptions don't bubble up into the UI thread
                System.err.println("Plugin toolbar action failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        return btn;
    }

	private void initializePluginManager() {
		try {
			// Use the plugins directory from AppDirectories
			Path pluginsDir = AppDirectories.getPluginsDir();
			if (!Files.exists(pluginsDir)) {
				Files.createDirectories(pluginsDir);
			}
			pluginManager = new PluginManager(pluginsDir, false);
			pluginManager.initialize();
		} catch (Exception e) {
			System.err.println("Failed to initialize plugin manager: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void showAlert(String title, String message) {
	    Alert alert = new Alert(Alert.AlertType.ERROR);
	    alert.setTitle(title);
	    alert.setHeaderText(null);
	    alert.setContentText(message);
	    alert.showAndWait();
	}

	public static void main(String[] args) {
		launch(args);
	}
}