package gameproject.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages player progress persistence
 */
public class ProgressTracker implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ProgressTracker.class.getName());
    private static final String SAVE_FILE = "smartsortstory_progress.dat";
    
    private static ProgressTracker instance;
    
    // Map of levelId to stars earned (1-3)
    private Map<String, Integer> completedLevels;
    private int totalStarsEarned;
    private String playerName;
    
    /**
     * Private constructor for singleton
     */
    private ProgressTracker() {
        completedLevels = new HashMap<>();
        totalStarsEarned = 0;
        playerName = "Player";
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ProgressTracker getInstance() {
        if (instance == null) {
            instance = loadProgress();
        }
        return instance;
    }
    
    /**
     * Generate a level ID from difficulty and level number
     */
    private String getLevelId(String difficulty, int levelNumber) {
        return difficulty + "_" + levelNumber;
    }
    
    /**
     * Check if a level is completed
     */
    public boolean isLevelCompleted(String difficulty, int levelNumber) {
        String levelId = getLevelId(difficulty, levelNumber);
        System.out.println("DEBUG: Checking if level is completed: " + levelId);
        return completedLevels.containsKey(levelId);
    }
    
    /**
     * Get stars earned for a level
     */
    public int getStarsForLevel(String difficulty, int levelNumber) {
        String levelId = getLevelId(difficulty, levelNumber);
        return completedLevels.getOrDefault(levelId, 0);
    }
    
    /**
     * Complete a level and earn stars
     */
    public void completeLevel(String difficulty, int levelNumber, int stars) {
        String levelId = getLevelId(difficulty, levelNumber);
        int currentStars = completedLevels.getOrDefault(levelId, 0);
        System.out.println("DEBUG: Completing level: " + levelId + " with stars: " + stars);

        // Only update if we earned more stars
        if (stars > currentStars) {
            totalStarsEarned += (stars - currentStars);
            completedLevels.put(levelId, stars);
            // CRITICAL FIX: Make sure to save progress immediately
            saveProgress();

            // Add extra debug output
            System.out.println("DEBUG: Progress updated for " + levelId + ", new stars: " + stars);
            System.out.println("DEBUG: Total stars earned: " + totalStarsEarned);
        }
    }
    
    /**
     * Get total stars earned
     */
    public int getTotalStarsEarned() {
        return totalStarsEarned;
    }
    
    /**
     * Set player name
     */
    public void setPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.playerName = name;
            saveProgress();
        }
    }
    
    /**
     * Get player name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Reset all progress
     */
    public void resetProgress() {
        completedLevels.clear();
        totalStarsEarned = 0;
        saveProgress();
    }
    
    /**
     * Save progress to file
     */
    public void saveProgress() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(this);
            LOGGER.info("Progress saved successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save progress", e);
        }
    }
    
    /**
     * Load progress from file
     */
    private static ProgressTracker loadProgress() {
        File saveFile = new File(SAVE_FILE);
        
        if (saveFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
                ProgressTracker tracker = (ProgressTracker) ois.readObject();
                LOGGER.info("Progress loaded successfully");
                return tracker;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to load progress", e);
            }
        }
        
        // Return new instance if file doesn't exist or there's an error
        return new ProgressTracker();
    }
}