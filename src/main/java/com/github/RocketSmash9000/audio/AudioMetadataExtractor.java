package com.github.RocketSmash9000.audio;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for extracting metadata from audio files.
 */
public class AudioMetadataExtractor {
    
    /**
     * Extracts the track title from the audio file's metadata.
     * If no title is found, returns the filename without extension.
     * 
     * @param file The audio file to extract metadata from
     * @return The track title, or filename if no title is found
     */
    public static String getTrackTitle(File file) {
        if (!file.getName().toLowerCase().endsWith(".mp3")) {
            return getFileNameWithoutExtension(file);
        }
        
        try {
            Mp3File mp3file = new Mp3File(file);
            String title = null;
            
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                title = id3v2Tag.getTitle();
            } 
            
            if ((title == null || title.trim().isEmpty()) && mp3file.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                title = id3v1Tag.getTitle();
            }
            
            if (title != null && !title.trim().isEmpty()) {
                return title.trim();
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            // If there's any error reading metadata, fall back to filename
        }
        
        return getFileNameWithoutExtension(file);
    }
    
    /**
     * Extracts the artist name from the audio file's metadata.
     * 
     * @param file The audio file to extract metadata from
     * @return The artist name, or empty string if not found
     */
    public static String getArtist(File file) {
        if (!file.getName().toLowerCase().endsWith(".mp3")) {
            return "";
        }
        
        try {
            Mp3File mp3file = new Mp3File(file);
            String artist = null;
            
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                artist = id3v2Tag.getArtist();
            } 
            
            if ((artist == null || artist.trim().isEmpty()) && mp3file.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                artist = id3v1Tag.getArtist();
            }
            
            if (artist != null && !artist.trim().isEmpty()) {
                return artist.trim();
            }
        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
            // If there's any error reading metadata, return empty string
        }
        return "";
    }
    
    /**
     * Gets a formatted string with the track title and artist.
     * Format: "Title - Artist" or just "Title" if no artist is available.
     * 
     * @param file The audio file to get the display name for
     * @return Formatted display name
     */
    public static String getDisplayName(File file) {
        String title = getTrackTitle(file);
        String artist = getArtist(file);
        
        if (artist == null || artist.isEmpty()) {
            return title;
        } else {
            return String.format("%s - %s", title, artist);
        }
    }
    
    /**
     * Gets the filename without its extension.
     * 
     * @param file The file to get the name from
     * @return The filename without extension
     */
    private static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }
}
