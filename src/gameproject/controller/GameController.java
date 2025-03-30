package gameproject.controller;

import gameproject.model.GameModel;
import gameproject.model.GameState;
import gameproject.model.LevelConfig;
import gameproject.model.ProgressTracker;
import gameproject.view.*;
import gameproject.util.ResourceManager;
import gameproject.util.GameConstants;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Enhanced controller class that handles game logic and user interactions
 */
public class GameController {
    private GameModel model;
    private ProgressTracker progressTracker;
    private ResourceManager resourceManager;
    private JFrame mainFrame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    private MainMenuView mainMenuView;
    private LevelSelectionView levelSelectionView;
    private StoryView storyView;
    private GameView gameView;
    
    private List<LevelConfig> allLevels;
    
    /**
     * Constructor - Initialize the game controller
     */
    public GameController() {
        // Initialize singletons
        this.resourceManager = ResourceManager.getInstance();
        this.progressTracker = ProgressTracker.getInstance();
        this.model = new GameModel();
        
        // Load all level configurations
        this.allLevels = LevelConfig.createAllLevels();
        
        // Set up the main frame
        mainFrame = new JFrame(GameConstants.GAME_TITLE);
        mainFrame.setSize(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        
        // Set up the main panel with card layout
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        
        // Initialize views
        initializeViews();
        
        // Add views to the card layout
        mainPanel.add(mainMenuView, "mainMenu");
        mainPanel.add(levelSelectionView, "levelSelection");
        mainPanel.add(storyView, "story");
        mainPanel.add(gameView, "game");
        
        // Add main panel to the frame
        mainFrame.add(mainPanel);
    }
    
    /**
     * Initialize all views
     */
    private void initializeViews() {
        mainMenuView = new MainMenuView(this);
        levelSelectionView = new LevelSelectionView(this);
        storyView = new StoryView(this);
        gameView = new GameView(this);
    }
    
    /**
     * Start the game application
     */
    public void startApplication() {
        // Show the main menu initially
        cardLayout.show(mainPanel, "mainMenu");
        mainFrame.setVisible(true);
    }
    
    /**
     * Show the main menu
     */
    public void showMainMenu() {
        model.setCurrentState(GameState.MAIN_MENU);
        cardLayout.show(mainPanel, "mainMenu");
    }
    
    /**
     * Show the level selection screen
     */
    public void showLevelSelection() {
        model.setCurrentState(GameState.LEVEL_SELECTION);
        levelSelectionView.updateLevelStatus();
        cardLayout.show(mainPanel, "levelSelection");
    }
    
    /**
     * Start the story mode
     */
    public void startGame() {
        model.setCurrentState(GameState.STORY_MODE);
        cardLayout.show(mainPanel, "story");
    }
    
    /**
     * Skip the story and go to the game
     */
    public void skipStory() {
        startLevel("Beginner", 1);
    }
    
    /**
     * Start the insertion sort challenge
     */
    public void startInsertionSortChallenge() {
        startLevel("Beginner", 1);
    }
    
    /**
     * Start a specific level
     */
    public void startLevel(String difficulty, int level) {
        model.setCurrentLevel(level);
        model.setCurrentDifficulty(difficulty);
        
        // Set game state based on level configuration
        for (LevelConfig config : allLevels) {
            if (config.getDifficulty().equals(difficulty) && config.getLevelNumber() == level) {
                model.setCurrentState(config.getAlgorithmType());
                break;
            }
        }
        
        gameView.updateLevelInfo(difficulty, level);
        cardLayout.show(mainPanel, "game");
    }
    
    /**
     * Restart the current level
     */
    public void restartLevel() {
        startLevel(model.getCurrentDifficulty(), model.getCurrentLevel());
    }
    
    /**
     * Go to the next level
     */
    public void goToNextLevel() {
        String currentDifficulty = model.getCurrentDifficulty();
        int currentLevel = model.getCurrentLevel();
        
        // Find the next level
        boolean foundCurrent = false;
        boolean foundNext = false;
        
        for (LevelConfig config : allLevels) {
            if (foundCurrent && !foundNext) {
                // This is the next level
                startLevel(config.getDifficulty(), config.getLevelNumber());
                foundNext = true;
                break;
            }
            
            if (config.getDifficulty().equals(currentDifficulty) && 
                config.getLevelNumber() == currentLevel) {
                foundCurrent = true;
            }
        }
        
        if (!foundNext) {
            // No more levels, go back to level selection
            JOptionPane.showMessageDialog(mainFrame,
                    "Congratulations! You've completed all available levels!",
                    "Game Complete", JOptionPane.INFORMATION_MESSAGE);
            showLevelSelection();
        }
    }
    
    /**
     * Show a hint for the current level
     */
    public void showHint() {
        // Find the current level configuration
        for (LevelConfig config : allLevels) {
            if (config.getDifficulty().equals(model.getCurrentDifficulty()) && 
                config.getLevelNumber() == model.getCurrentLevel()) {
                
                JOptionPane.showMessageDialog(mainFrame,
                    config.getHint(),
                    "Hint", JOptionPane.INFORMATION_MESSAGE);
                break;
            }
        }
    }
    
    /**
     * Record completion of a level with stars
     */
    public void completeLevelWithStars(String difficulty, int level, int stars) {
        progressTracker.completeLevel(difficulty, level, stars);
    }
    
    /**
     * Check if a level is unlocked
     */
    public boolean isLevelUnlocked(String difficulty, int level) {
        // First level of each difficulty is always unlocked
        if (level == 1) {
            return true;
        }

        // Previous level must be completed
        return progressTracker.isLevelCompleted(difficulty, level - 1);
    }  // <-- Add this closing brace

    
    
    
    
    /**
    * Show help information
    */
   public void showHelp() {
       JOptionPane.showMessageDialog(mainFrame,
           "SmartSortStory is an interactive game to learn sorting algorithms.\n\n" +
           "- Drag and drop elements to sort them (Insertion Sort)\n" +
           "- Merge sorted runs by selecting correct pairs (Merge Sort)\n" +
           "- Progress through levels from Beginner to Advanced\n" +
           "- Follow the story to complete the game\n\n" +
           "Each level can earn up to 3 stars based on your performance!",
           "How to Play", JOptionPane.INFORMATION_MESSAGE);
   }

   /**
    * Exit the game
    */
   public void exitGame() {
       int response = JOptionPane.showConfirmDialog(mainFrame,
               "Are you sure you want to exit? Your progress is saved.",
               "Exit Game", JOptionPane.YES_NO_OPTION);

       if (response == JOptionPane.YES_OPTION) {
           // If you've implemented ProgressTracker, uncomment this line
           // ProgressTracker.getInstance().saveProgress();
           System.exit(0);
       }
   }
}