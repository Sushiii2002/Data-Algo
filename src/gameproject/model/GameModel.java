package gameproject.model;

/**
 * Represents the game's data and state
 */
public class GameModel {
    private GameState currentState;
    private int currentLevel;
    private String currentDifficulty;

    public GameModel() {
        this.currentState = GameState.MAIN_MENU;
        this.currentLevel = 1;
        this.currentDifficulty = "Beginner";
    }

    // Getters and setters
    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState currentState) {
        this.currentState = currentState;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public String getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void setCurrentDifficulty(String currentDifficulty) {
        this.currentDifficulty = currentDifficulty;
    }
}