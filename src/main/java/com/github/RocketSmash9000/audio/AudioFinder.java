package com.github.RocketSmash9000.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Class used for finding all audio files inside a directory.
 */
public class AudioFinder {
	private static final List<String> SUPPORTED_AUDIO_EXTENSIONS = List.of(
			".mp3", ".wav", ".ogg", ".m4a", ".flac", ".aac"
	);

	private final List<File> audioFiles;

	/**
	 * Creates a new AudioFinder that will scan the specified directory for audio files.
	 * @param directoryPath The path to the directory to scan
	 * @throws IllegalArgumentException if the path is not a valid directory
	 */
	public AudioFinder(String directoryPath) {
		Objects.requireNonNull(directoryPath, "Directory path cannot be null");

		File directory = new File(directoryPath);
		if (!directory.isDirectory()) {
			throw new IllegalArgumentException("Path must be a valid directory: " + directoryPath);
		}

		this.audioFiles = Collections.unmodifiableList(findAudioFiles(directory));
	}

	/**
	 * Returns an unmodifiable list of found audio files.
	 * @return List of audio files
	 */
	public List<File> getAudioFiles() {
		return audioFiles;
	}

	private List<File> findAudioFiles(File directory) {
		List<File> foundFiles = new ArrayList<>();
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					foundFiles.addAll(findAudioFiles(file)); // Recursively search subdirectories
				} else if (isAudioFile(file)) {
					foundFiles.add(file);
				}
			}
		}

		return foundFiles;
	}

	private boolean isAudioFile(File file) {
		if (file == null || !file.isFile()) {
			return false;
		}

		String fileName = file.getName().toLowerCase();
		return SUPPORTED_AUDIO_EXTENSIONS.stream()
				.anyMatch(fileName::endsWith);
	}
}