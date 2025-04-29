package gameproject.controller;

import gameproject.model.GameModel;
import gameproject.model.GameState;
import gameproject.model.LevelConfig;
import gameproject.model.ProgressTracker;
import gameproject.model.NarrativeSystem;
import gameproject.view.*;
import gameproject.ui.TimSortVisualization; // This import is correct
import gameproject.util.ResourceManager;
import gameproject.util.GameConstants;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Enhanced controller class that integrates the narrative system for the TimSort RPG
 * Modified to remove timer and lives system from visualization
 */
public class GameController {
    private GameModel model;
    private ProgressTracker progressTracker;
    private ResourceManager resourceManager;
    private NarrativeSystem narrativeSystem;
    
    private JFrame mainFrame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    private MainMenuView mainMenuView;
    private LevelSelectionView levelSelectionView;
    private EnhancedStoryView enhancedStoryView;
    private GameView gameView;
    private TimSortVisualization timSortVisualization;
    
    private List<LevelConfig> allLevels;
    
    /**
     * Constructor - Initialize the game controller
     */
    public GameController() {
        // Initialize singletons
        this.resourceManager = ResourceManager.getInstance();
        this.progressTracker = ProgressTracker.getInstance();
        this.narrativeSystem = NarrativeSystem.getInstance();
        this.model = new GameModel();
        
        // Initialize narrative system with controller reference
        this.narrativeSystem.initialize(this);
        
        // Load all level configurations
        this.allLevels = LevelConfig.createAllLevels();
        
        // Set up the main frame
        mainFrame = new JFrame(GameConstants.GAME_TITLE);
        mainFrame.setSize(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        mainFrame.setResizable(false);
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
        mainPanel.add(enhancedStoryView, "enhancedStory");
        mainPanel.add(gameView, "game");
        mainPanel.add(timSortVisualization, "timSortVisualization");
        
        // Add main panel to the frame
        mainFrame.add(mainPanel);
    }
    
    /**
     * Initialize all views
     */
    private void initializeViews() {
        mainMenuView = new MainMenuView(this);
        levelSelectionView = new LevelSelectionView(this);
        enhancedStoryView = new EnhancedStoryView(this);
        gameView = new GameView(this);
        timSortVisualization = new TimSortVisualization(this);
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
     * Start the story mode with enhanced narrative
     */
    public void startGame() {
        model.setCurrentState(GameState.STORY_MODE);
        cardLayout.show(mainPanel, "enhancedStory");
        enhancedStoryView.startStory();
    }
    
    /**
     * Skip the story and go directly to Phase 1
     */
    public void skipStory() {
        startPhaseGameplay(1);
    }
    
    /**
     * Start a specific algorithm phase gameplay
     */
    public void startPhaseGameplay(int phase) {
        switch (phase) {
            case 1:
                // Eye of Pattern phase
                model.setCurrentState(GameState.TIMSORT_CHALLENGE);
                model.setCurrentLevel(1);

                // Show TimSort visualization for phase 1
                timSortVisualization.setPhase(1); // Explicitly set phase
                cardLayout.show(mainPanel, "timSortVisualization");
                break;

            case 2:
                // Hand of Balance phase
                model.setCurrentState(GameState.TIMSORT_CHALLENGE);
                model.setCurrentLevel(2);

                // Show TimSort visualization for phase 2
                timSortVisualization.setPhase(2); // Explicitly set phase
                cardLayout.show(mainPanel, "timSortVisualization");
                break;

            case 3:
                // Mind of Unity phase
                model.setCurrentState(GameState.TIMSORT_CHALLENGE);
                model.setCurrentLevel(3);

                // Show TimSort visualization for phase 3
                timSortVisualization.setPhase(3); // Explicitly set phase
                cardLayout.show(mainPanel, "timSortVisualization");
                break;

            default:
                // Invalid phase - show story view
                model.setCurrentState(GameState.STORY_MODE);
                cardLayout.show(mainPanel, "enhancedStory");
                break;
        }
    }

    
    /**
     * Handle dialogue sequence completion
     */
    public void onDialogueSequenceEnded() {
        // Check current game state to determine action
        if (model.getCurrentState() == GameState.STORY_MODE) {
            // Determine next action based on narrative system state
            GameState nextPhase = narrativeSystem.getCurrentAlgorithmPhase();
            
            if (nextPhase != null) {
                model.setCurrentState(nextPhase);
                
                // Determine which phase to start
                if (nextPhase == GameState.TIMSORT_CHALLENGE) {
                    int phaseNumber = model.getCurrentLevel();
                    startPhaseGameplay(phaseNumber);
                }
            }
        }
    }
    
    /**
     * Return to story mode after completing a phase
     */
    public void returnToStoryMode() {
        model.setCurrentState(GameState.STORY_MODE);
        cardLayout.show(mainPanel, "enhancedStory");
        
        // Get current phase for dialogue key
        int currentPhase = model.getCurrentLevel();
        String dialogueKey = "phase" + currentPhase + "_end";
        
        // Show appropriate dialogue
        enhancedStoryView.startPhaseDialogue(currentPhase - 1, dialogueKey);
        
        // Increment current level to prepare for next phase
        model.setCurrentLevel(currentPhase + 1);
    }
    
    /**
     * Handle boss battle completion
     */
    public void onBossBattleComplete(boolean success, int bossLevel) {
        model.setCurrentState(GameState.STORY_MODE);
        cardLayout.show(mainPanel, "enhancedStory");
        
        // Show appropriate dialogue based on outcome
        enhancedStoryView.showBossBattleResult(success, bossLevel);
        
        // Record progress if successful
        if (success) {
            progressTracker.completeLevel("Beginner", bossLevel, 3);
        }
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
        
        // For Level 1, start the story
        if (difficulty.equals("Beginner") && level == 1) {
            startGame();
        } else {
            // For other levels, go directly to game view
            gameView.updateLevelInfo(difficulty, level);
            cardLayout.show(mainPanel, "game");
        }
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
        // If in TimSort visualization, use the narrative system's hints
        if (model.getCurrentState() == GameState.TIMSORT_CHALLENGE) {
            String hint = narrativeSystem.getCurrentHint();
            JOptionPane.showMessageDialog(mainFrame, hint, "Hint", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Otherwise, look up hint in level config
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
    }
    
    /**
     * Show help information
     */
    public void showHelp() {
        JOptionPane.showMessageDialog(mainFrame,
            "The Alchemist's Path is an interactive RPG that teaches the TimSort algorithm through potion crafting.\n\n" +
            "Three essential abilities you'll master:\n" +
            "- The Eye of Pattern: Identify natural sequences in ingredients\n" +
            "- The Hand of Balance: Sort small groups of ingredients\n" +
            "- The Mind of Unity: Merge ordered ingredients to craft potions\n\n" +
            "Create the right potions to defeat three powerful bosses:\n" +
            "- Flameclaw: A fire elemental that burns everything\n" +
            "- Toxitar: A poison beast that spreads corruption\n" +
            "- Lord Chaosa: A reality-warping final boss\n\n" +
            "Follow the story and character hints to choose the correct potions!",
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
            progressTracker.saveProgress();
            System.exit(0);
        }
    }
}



