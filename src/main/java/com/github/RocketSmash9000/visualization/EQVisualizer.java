package com.github.RocketSmash9000.visualization;

import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EQVisualizer extends Pane {
    private static final int BANDS = 30;
    private static final double UPDATE_INTERVAL = 0.05; // 50ms
    private static final double MIN_BAR_WIDTH = 10.0; // Minimum width of each bar in pixels
    private static final double BAR_SPACING = 5.0; // Spacing between bars in pixels
    private final List<Line> eqBars = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private double width;
    private final double height;
    private double lastUpdateTime = 0;
    private final float[] magnitudes = new float[BANDS];
    private boolean isEnabled = false;

    public EQVisualizer(MediaPlayer mediaPlayer, double width, double height) {
        this.mediaPlayer = mediaPlayer;
        this.width = width;
        this.height = height;
        
        // Make the visualization fill its container
        setMaxWidth(Double.MAX_VALUE);
        setMinWidth(0);
        
        // Set up animation
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateBars();
            }
        }.start();
        
        // Listen for width changes
        widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                this.width = newVal.doubleValue();
                recreateBars();
            }
        });
        
        // Set up audio spectrum listener
        if (mediaPlayer != null) {
            setMediaPlayer(mediaPlayer);
        }
        
        // Initial bar creation
        recreateBars();
    }

	@SuppressWarnings("D")
    public void setMediaPlayer(MediaPlayer newMediaPlayer) {
        // Remove listener from old media player if exists
        if (this.mediaPlayer != null) {
            try {
                this.mediaPlayer.setAudioSpectrumListener(null);
            } catch (Exception e) {
                // Ignore errors when cleaning up old media player
            }
        }
        
        this.mediaPlayer = newMediaPlayer;
        
        if (newMediaPlayer != null) {
            try {
                // Set up audio spectrum listener
                newMediaPlayer.setAudioSpectrumListener((double timestamp, double duration, 
                        float[] magnitudes, float[] phases) -> {
                    if (isEnabled && magnitudes != null && magnitudes.length > 0) {
                        // Average the bands to make the visualization smoother
                        int bandsPerBar = Math.max(1, magnitudes.length / BANDS);
                        for (int i = 0; i < BANDS && i * bandsPerBar < magnitudes.length; i++) {
                            float sum = 0;
                            int start = i * bandsPerBar;
                            int end = Math.min(start + bandsPerBar, magnitudes.length);
                            for (int j = start; j < end; j++) {
                                sum += magnitudes[j];
                            }
                            this.magnitudes[i] = sum / (end - start);
                        }
                    }
                });
                
                // Set spectrum interval (in seconds)
                newMediaPlayer.setAudioSpectrumInterval(UPDATE_INTERVAL);
                // Number of bands (must be a power of 2, between 2 and 32768)
                newMediaPlayer.setAudioSpectrumNumBands(256);
                // Sensitivity in decibels (negative values)
                newMediaPlayer.setAudioSpectrumThreshold(-60);
            } catch (Exception e) {
                System.err.println("Error setting up audio spectrum: " + e.getMessage());
            }
        }
    }
    
    private void recreateBars() {
        getChildren().clear();
        eqBars.clear();
        
        if (width <= 0) return;
        
        // Calculate how many bars can fit with minimum width and spacing
        int maxBands = (int) Math.max(1, (width + BAR_SPACING) / (MIN_BAR_WIDTH + BAR_SPACING));
        
        // Calculate actual bar width to fill available space
        double barWidth = (width - ((maxBands - 1) * BAR_SPACING)) / maxBands;
        
        // Calculate total width used by all bars and spacing
        double totalWidth = (barWidth * maxBands) + (BAR_SPACING * (maxBands - 1));
        
        // Calculate start position to center the bars
        double startX = (width - totalWidth) / 2;
        
        // Create bars
        for (int i = 0; i < maxBands; i++) {
            Line bar = new Line();
            bar.setStroke(Color.rgb(100, 200, 255));
            bar.setStrokeWidth(barWidth);
            
            // Position each bar with proper spacing
            double x = startX + (i * (barWidth + BAR_SPACING)) + (barWidth / 2);
            
            bar.setStartX(x);
            bar.setStartY(height);
            bar.setEndX(x);
            bar.setEndY(height);
            
            eqBars.add(bar);
            getChildren().add(bar);
        }
        
        // Add background
        addBackground();
    }
    
    private void addBackground() {
        // Add a semi-transparent background for better visibility
        Rectangle bg = new Rectangle(0, 0, width, height);
        bg.setFill(Color.rgb(0, 0, 0, 0.3));
        getChildren().add(0, bg);
    }
    
    private void updateBars() {
        if (!isEnabled || eqBars.isEmpty()) return;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < 50) { // Limit to ~20 FPS
            return;
        }
        lastUpdateTime = currentTime;
        
        int bandsToUse = Math.min(eqBars.size(), magnitudes.length);
        if (bandsToUse == 0) return;
        
        // Distribute the frequency data across the current number of bars
        int bandsPerBar = Math.max(1, magnitudes.length / eqBars.size());
        
        for (int i = 0; i < eqBars.size() && (i * bandsPerBar) < magnitudes.length; i++) {
            Line bar = eqBars.get(i);
            
            // Average the magnitudes for this bar
	        double normalizedValue = getNormalizedValue(i, bandsPerBar);

	        double barHeight = normalizedValue * height * 0.9; // 90% of the height
            
            bar.setStartY(height);
            bar.setEndY(height - barHeight);
            
            // Change color based on intensity (blue to cyan)
            double hue = 180 + (normalizedValue * 180);
            bar.setStroke(Color.hsb(hue, 0.8, 1.0));
        }
    }

	private double getNormalizedValue(int i, int bandsPerBar) {
		double sum = 0;
		int start = i * bandsPerBar;
		int end = Math.min(start + bandsPerBar, magnitudes.length);
		for (int j = start; j < end; j++) {
		    sum += magnitudes[j];
		}
		double avgMagnitude = sum / (end - start);

		// Convert dB to a value between 0 and 1 (with some scaling)
		double normalizedValue = (avgMagnitude + 60) / 60.0;
		// Clamp the value between 0 and 1
		normalizedValue = Math.max(0, Math.min(1, normalizedValue));
		// Apply a curve to make the visualization more dynamic
		normalizedValue = Math.pow(normalizedValue, 1.5);
		return normalizedValue;
	}

	public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        setVisible(enabled);
        
        if (enabled) {
            // Reset magnitudes when enabling
	        // Min value
	        Arrays.fill(magnitudes, -60);
        }
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
}
