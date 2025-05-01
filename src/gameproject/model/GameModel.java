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
    
    // Add these fields to GameModel class
    private String leftPotionType = "Fire Resistance";  // Default value
    private String rightPotionType = "Strength";        // Default value
    private String selectedPotion = null;     
    private boolean bossBattleCompleted = false;
    
    private int gameLevel = 1; // Default to Level 1
    
    
    
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
    
    
    /**
    * Get the left potion type
    */
    public String getLeftPotionType() {
        return leftPotionType;
    }

    /**
     * Set the left potion type
     */
    public void setLeftPotionType(String type) {
        this.leftPotionType = type;
    }

    /**
     * Get the right potion type
     */
    public String getRightPotionType() {
        return rightPotionType;
    }

    /**
     * Set the right potion type
     */
    public void setRightPotionType(String type) {
        this.rightPotionType = type;
    }

    /**
     * Get the selected potion
     */
    public String getSelectedPotion() {
        return selectedPotion;
    }

    /**
     * Set the selected potion
     */
    public void setSelectedPotion(String potion) {
        this.selectedPotion = potion;
    }
    
    
    public boolean isBossBattleCompleted() {
        return bossBattleCompleted;
    }

    public void setBossBattleCompleted(boolean completed) {
        this.bossBattleCompleted = completed;
    }
    
    /**
    * Get the current game level (1, 2, or 3)
    */
    public int getGameLevel() {
        return gameLevel;
    }
    
    
    /**
    * Set the current game level
    */
    public void setGameLevel(int level) {
        if (level >= 1 && level <= 3) {
            this.gameLevel = level;
        }
    }
    
    
    /**
    * Get the current boss name based on game level
    */
    public String getCurrentBossName() {
        switch (gameLevel) {
            case 1:
                return "Flameclaw";
            case 2:
                return "Toxitar";
            case 3:
                return "LordChaosa";
            default:
                return "Flameclaw"; // Default
        }
    }
    
    
    
    /**
    * Get the correct potion type for the current boss
    */
    public String getCorrectPotionForCurrentBoss() {
        switch (gameLevel) {
            case 1:
                return "Fire Resistance Potion";
            case 2:
                return "Dexterity Potion";
            case 3:
                return "Cold Resistance Potion";
            default:
                return "Fire Resistance Potion"; // Default
        }
    }
}