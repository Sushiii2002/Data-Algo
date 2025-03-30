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
    
    /**
     * Constructor
     */
    public LevelConfig(String difficulty, int levelNumber, GameState algorithmType, 
            String instruction, int[] initialArray, int maxSteps, String hint) {
        this.difficulty = difficulty;
        this.levelNumber = levelNumber;
        this.algorithmType = algorithmType;
        this.instruction = instruction;
        this.initialArray = initialArray;
        this.maxSteps = maxSteps;
        this.hint = hint;
        
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
    
    /**
     * Factory method to create level configurations for all levels
     */
    public static List<LevelConfig> createAllLevels() {
        List<LevelConfig> levels = new ArrayList<>();
        
        // Beginner Levels
        levels.add(new LevelConfig(
            "Beginner", 
            1, 
            GameState.INSERTION_SORT_CHALLENGE,
            "Sort these numbers using Insertion Sort. Move the smaller numbers to the left.",
            new int[]{5, 2, 9, 1, 6},
            10,
            "Start by moving the 2 before the 5, then place 9, then move 1 to the beginning."
        ));
        
        levels.add(new LevelConfig(
            "Beginner", 
            2, 
            GameState.INSERTION_SORT_CHALLENGE,
            "Continue practicing Insertion Sort with this larger set of numbers.",
            new int[]{8, 4, 7, 3, 10, 2, 6},
            15,
            "Remember to compare each element with all previous elements and insert it in the correct position."
        ));
        
        levels.add(new LevelConfig(
            "Beginner", 
            3, 
            GameState.MERGE_SORT_CHALLENGE,
            "Learn Merge Sort by combining these pre-sorted pairs of numbers.",
            new int[]{3, 7, 1, 5, 2, 8, 4, 6},
            12,
            "First merge pairs into sorted runs of length 2, then merge those into runs of length 4."
        ));
        
        // Intermediate Levels
        levels.add(new LevelConfig(
            "Intermediate", 
            1, 
            GameState.MERGE_SORT_CHALLENGE,
            "Apply Merge Sort to divide and conquer this unsorted array.",
            new int[]{12, 7, 3, 9, 15, 2, 6, 8, 1},
            20,
            "Divide the array into halves recursively until you have single elements, then merge them back."
        ));
        
        levels.add(new LevelConfig(
            "Intermediate", 
            2, 
            GameState.INSERTION_SORT_CHALLENGE,
            "Apply Insertion Sort with a time constraint. Can you sort in under 45 seconds?",
            new int[]{15, 8, 12, 3, 7, 9, 1, 11, 14, 5},
            25,
            "Focus on moving elements efficiently - don't make unnecessary swaps."
        ));
        
        levels.add(new LevelConfig(
            "Intermediate", 
            3, 
            GameState.TIMSORT_CHALLENGE,
            "Learn TimSort by first applying Insertion Sort to small runs, then merging.",
            new int[]{9, 3, 7, 2, 12, 8, 4, 10, 5, 11, 1, 6},
            30,
            "First identify 'runs' of 4 elements and sort them with Insertion Sort, then merge the runs."
        ));
        
        // Advanced Levels
        levels.add(new LevelConfig(
            "Advanced", 
            1, 
            GameState.TIMSORT_CHALLENGE,
            "Master TimSort with this large unsorted array. Sort small runs, then merge efficiently.",
            new int[]{14, 8, 17, 3, 10, 19, 5, 12, 7, 16, 2, 13, 9, 18, 4, 11, 6, 15, 1},
            40,
            "Find natural runs in the data, and use that to your advantage for more efficient sorting."
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