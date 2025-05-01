// src/gameproject/model/LevelConfig.java

package gameproject.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to manage level configurations
 */
public class LevelConfig {
    private String difficulty;
    private int levelNumber;
    private GameState algorithmType;
    private String instruction;
    private int[] initialArray;
    private int[] targetArray;
    private int maxSteps;
    private int timeLimit; // in seconds (0 for no limit)
    private String hint;
    private String backgroundTheme; // Forest, Mountains, Desert
    private int gridSize; // Size of the grid (e.g., 5x5)
    
    /**
     * Constructor
     */
    public LevelConfig(String difficulty, int levelNumber, GameState algorithmType, 
            String instruction, int[] initialArray, int maxSteps, String hint, 
            String backgroundTheme, int gridSize) {
        this.difficulty = difficulty;
        this.levelNumber = levelNumber;
        this.algorithmType = algorithmType;
        this.instruction = instruction;
        this.initialArray = initialArray;
        this.maxSteps = maxSteps;
        this.hint = hint;
        this.backgroundTheme = backgroundTheme;
        this.gridSize = gridSize;
        
        // Target array is always the sorted version of initial array
        this.targetArray = initialArray.clone();
        java.util.Arrays.sort(this.targetArray);
    }
    
    // Getters and setters for all properties
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public int getLevelNumber() {
        return levelNumber;
    }
    
    public GameState getAlgorithmType() {
        return algorithmType;
    }
    
    public String getInstruction() {
        return instruction;
    }
    
    public int[] getInitialArray() {
        return initialArray.clone();
    }
    
    public int[] getTargetArray() {
        return targetArray.clone();
    }
    
    public int getMaxSteps() {
        return maxSteps;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    public String getHint() {
        return hint;
    }
    
    public String getBackgroundTheme() {
        return backgroundTheme;
    }
    
    public int getGridSize() {
        return gridSize;
    }
    
    /**
     * Factory method to create level configurations for all levels
     */
    public static List<LevelConfig> createAllLevels() {
        List<LevelConfig> levels = new ArrayList<>();
        
        // Beginner Levels with different background themes
        levels.add(new LevelConfig(
            "Beginner", 
            1, 
            GameState.INSERTION_SORT_CHALLENGE,
            "Sort these items by value using Insertion Sort. Move the smaller items to the left.",
            new int[]{5, 2, 9, 1, 6},
            10,
            "Start by moving the smaller items to the left. Look at the hint for each item to see its value.",
            "Forest",
            5  // 5x5 grid
        ));
        
        levels.add(new LevelConfig(
            "Beginner", 
            2, 
            GameState.INSERTION_SORT_CHALLENGE,
            "Continue practicing Insertion Sort with these items. Arrange from lightest to heaviest.",
            new int[]{8, 4, 7, 3, 10, 2, 6},
            15,
            "Remember to compare each item with all previous items and insert it in the correct position.",
            "Mountains",
            5
        ));
        
        levels.add(new LevelConfig(
            "Beginner", 
            3, 
            GameState.MERGE_SORT_CHALLENGE,
            "Learn Merge Sort by combining weapon and armor categories into organized sets.",
            new int[]{3, 7, 1, 5, 2, 8, 4, 6},
            12,
            "First group weapons together and armor together, then merge these sorted categories.",
            "Desert",
            5
        ));
        
        // Intermediate Levels - Introducing TimSort with item combinations
        levels.add(new LevelConfig(
            "Intermediate", 
            1, 
            GameState.TIMSORT_CHALLENGE,
            "Create item combinations to demonstrate TimSort. Combine bow + arrow, sword + shield, and other matching pairs.",
            new int[]{12, 7, 3, 9, 15, 2, 6, 8, 1},
            20,
            "TimSort first identifies small 'runs' of items that go together. Try combining the matching item pairs.",
            "Forest",
            5
        ));
        
        levels.add(new LevelConfig(
            "Intermediate", 
            2, 
            GameState.TIMSORT_CHALLENGE,
            "Continue with TimSort by combining basic items into equipment sets, then merging those sets.",
            new int[]{15, 8, 12, 3, 7, 9, 1, 11, 14, 5},
            25,
            "First create the basic equipment combinations, then merge those into class equipment sets.",
            "Mountains",
            5
        ));
       
        
        
        levels.add(new LevelConfig(
            "Intermediate", 
            3, 
            GameState.TIMSORT_CHALLENGE,
            "Master TimSort by creating nested combinations. Combine basic items, then merge the results.",
            new int[]{9, 3, 7, 2, 12, 8, 4, 10, 5, 11, 1, 6},
            30,
            "TimSort works by finding naturally occurring patterns and exploiting them. Try to identify which items naturally go together.",
            "Desert",
            5
        ));
        
        // Advanced Levels - Advanced TimSort demonstrations
        levels.add(new LevelConfig(
            "Advanced", 
            1, 
            GameState.TIMSORT_CHALLENGE,
            "Complete a full adventure by combining items into sets, then combining those sets into larger groups using TimSort principles.",
            new int[]{14, 8, 17, 3, 10, 19, 5, 12, 7, 16, 2, 13, 9, 18, 4, 11, 6, 15, 1},
            40,
            "TimSort first creates small runs using Insertion Sort, then merges those runs efficiently. Follow the same pattern with the items.",
            "Forest",
            5
        ));
        
        levels.add(new LevelConfig(
            "Advanced", 
            1, 
            GameState.TIMSORT_CHALLENGE,
            "Use TimSort principles to craft a Strength potion capable of breaking through Lord Chaosa's reality distortions.",
            new int[]{14, 7, 11, 5, 8, 16, 9, 12, 3, 10, 18, 6},
            30,
            "Focus on ingredients that enhance physical strength. Raw power is needed to break through reality distortions.",
            "Mountains",
            5
        ));
        
        // Set time limits for intermediate and advanced levels
        for (LevelConfig level : levels) {
            if (level.getDifficulty().equals("Intermediate")) {
                level.setTimeLimit(45);
            } else if (level.getDifficulty().equals("Advanced")) {
                level.setTimeLimit(60);
            }
        }
        
        return levels;
    }
    
    /**
     * Validates if a solution is correct
     */
    public boolean validateSolution(int[] solution) {
        // For TimSort challenges, we need a special validation based on item combinations
        if (algorithmType == GameState.TIMSORT_CHALLENGE) {
            // Check if the result contains the expected combined items
            // For simplicity, just check if values are in ascending order for now
            for (int i = 1; i < solution.length; i++) {
                if (solution[i] < solution[i-1]) {
                    return false;
                }
            }
            return true;
        } else {
            // For insertion and merge sort, just check against sorted target array
            if (solution.length != targetArray.length) {
                return false;
            }
            
            for (int i = 0; i < solution.length; i++) {
                if (solution[i] != targetArray[i]) {
                    return false;
                }
            }
            
            return true;
        }
    }
}