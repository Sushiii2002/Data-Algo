package gameproject.model;

/**
 * Represents the data model for the game
 */
public class GameModel {
    // Constants
    public static final String GAME_TITLE = "SmartSortStory";
    public static final int WINDOW_WIDTH = 900;
    public static final int WINDOW_HEIGHT = 600;
    
    // Game state
    private GameState currentState;
    private int currentLevel;
    private String currentDifficulty;
    
    /**
     * Constructor - Initialize the game model
     */
    public GameModel() {
        this.currentState = GameState.MAIN_MENU;
        this.currentLevel = 1;
        this.currentDifficulty = "Beginner";
    }
    
    /**
     * Get the current game state
     */
    public GameState getCurrentState() {
        return currentState;
    }
    
    /**
     * Set the current game state
     */
    public void setCurrentState(GameState state) {
        this.currentState = state;
    }
    
    /**
     * Get the current level
     */
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * Set the current level
     */
    public void setCurrentLevel(int level) {
        this.currentLevel = level;
    }
    
    /**
     * Get the current difficulty
     */
    public String getCurrentDifficulty() {
        return currentDifficulty;
    }
    
    /**
     * Set the current difficulty
     */
    public void setCurrentDifficulty(String difficulty) {
        this.currentDifficulty = difficulty;
    }
}