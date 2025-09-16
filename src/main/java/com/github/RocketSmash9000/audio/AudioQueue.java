package com.github.RocketSmash9000.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * A queue that manages the playback order of audio files.
 * It maintains a list of files and provides methods to get the next random track.
 * When the queue is empty, it automatically refills itself with the original list.
 */
public class AudioQueue {
	private final List<File> originalFiles;
	private List<File> currentQueue;
	private final Random random;

	/**
	 * Creates a new AudioQueue with the given list of audio files.
	 * @param audioFiles The list of audio files to manage
	 * @throws IllegalArgumentException if audioFiles is null or empty
	 */
	public AudioQueue(List<File> audioFiles) {
		Objects.requireNonNull(audioFiles, "Audio files list cannot be null");
		if (audioFiles.isEmpty()) {
			throw new IllegalArgumentException("Audio files list cannot be empty");
		}

		// Create a defensive copy of the original list
		this.originalFiles = new ArrayList<>(audioFiles);
		this.currentQueue = new ArrayList<>(audioFiles);
		this.random = new Random();
	}

	/**
	 * Gets the next random audio file from the queue.
	 * If the queue is empty, it will be refilled with the original files.
	 * @return The next audio file to play, or null if no files are available
	 */
	public synchronized File getNextTrack() {
		if (currentQueue.isEmpty()) {
			refillQueue();
		}

		if (currentQueue.isEmpty()) {
			return null; // Should not happen if constructor validation passes
		}

		int randomIndex = random.nextInt(currentQueue.size());
		return currentQueue.remove(randomIndex);
	}

	/**
	 * Refills the current queue with all original files.
	 * The new queue will be a shuffled version of the original files.
	 */
	public synchronized void refillQueue() {
		currentQueue = new ArrayList<>(originalFiles);
		Collections.shuffle(currentQueue, random);
	}

	/**
	 * Gets the number of tracks remaining in the current queue.
	 * @return The number of tracks remaining
	 */
	public synchronized int getRemainingTracks() {
		return currentQueue.size();
	}

	/**
	 * Gets the total number of unique tracks in the queue.
	 * @return The total number of tracks
	 */
	public int getTotalTracks() {
		return originalFiles.size();
	}
}