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
    public GameModel model;
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
    private boolean inLevelTransition = false;
    
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
        // Reset TimSort visualization to prevent it from showing up later
        timSortVisualization.resetAllPhases();

        // Set game state to level selection
        model.setCurrentState(GameState.LEVEL_SELECTION);

        // Update level status
        levelSelectionView.updateLevelStatus();

        // Show the level selection view
        cardLayout.show(mainPanel, "levelSelection");
    }

    /**
     * Start the story mode with enhanced narrative
     */
    public void startGame() {
        // Reset transition flag
        inLevelTransition = false;

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
        // Prevent recursive calls or multiple transitions
        if (inLevelTransition) {
            System.out.println("DEBUG: Already in level transition, ignoring call to startPhaseGameplay");
            return;
        }

        inLevelTransition = true;
        System.out.println("DEBUG: Starting Phase " + phase + " gameplay for level " + model.getGameLevel());

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
    
    // Reset transition flag after a short delay
    Timer resetTimer = new Timer(500, e -> {
        inLevelTransition = false;
    });
    resetTimer.setRepeats(false);
    resetTimer.start();
}

    
    /**
     * Handle dialogue sequence completion
     */
    public void onDialogueSequenceEnded() {
        // Reset transition flag
        inLevelTransition = false;

        // Check current game state to determine action
        if (model.getCurrentState() == GameState.STORY_MODE) {
            // Get the current game level
            int gameLevel = model.getGameLevel();

            // Get current narrative phase
            GameState nextPhase = narrativeSystem.getCurrentAlgorithmPhase();

            System.out.println("DEBUG: Dialogue ended. Game Level: " + gameLevel + ", Next Phase: " + nextPhase);

            if (nextPhase != null) {
                model.setCurrentState(nextPhase);

                // Ensure we're starting with Phase 1 for the current level
                if (nextPhase == GameState.TIMSORT_CHALLENGE) {
                    // Start with phase 1 for whichever level we're on
                    int phaseNumber = 1;
                    model.setCurrentLevel(phaseNumber);

                    System.out.println("DEBUG: Starting gameplay for Level " + gameLevel + ", Phase " + phaseNumber);
                    startPhaseGameplay(phaseNumber);
                }
            } else {
                // Fallback if no phase is determined - start with Phase 1
                System.out.println("DEBUG: No phase determined, starting Phase 1");
                model.setCurrentState(GameState.TIMSORT_CHALLENGE);
                model.setCurrentLevel(1);
                startPhaseGameplay(1);
            }
        }
    }
    
    /**
     * Return to story mode after completing a phase
     */
    public void returnToStoryMode() {
        model.setCurrentState(GameState.STORY_MODE);
        cardLayout.show(mainPanel, "enhancedStory");

        // Get current phase and potion types
        int currentPhase = model.getCurrentLevel();
        String leftPotionType = model.getLeftPotionType();
        String rightPotionType = model.getRightPotionType();

        // Start dynamic dialogue based on phase and potion types
        enhancedStoryView.startDynamicPhaseDialogue(currentPhase);

        // Increment current level to prepare for next phase
        model.setCurrentLevel(currentPhase + 1);
    }
    
    /**
     * Handle boss battle completion
     */
    public void onBossBattleComplete(boolean success, int bossLevel) {
        model.setBossBattleCompleted(true);
        model.setCurrentState(GameState.STORY_MODE);

        // Add debug logging
        System.out.println("DEBUG: onBossBattleComplete called - success: " + success + ", bossLevel: " + bossLevel);

        // Get selected potion from the model
        String selectedPotion = model.getSelectedPotion();
        System.out.println("DEBUG: Selected potion: " + selectedPotion);

        // IMPORTANT: Show the story view FIRST before showing dialogue
        cardLayout.show(mainPanel, "enhancedStory");

        // Show appropriate dialogue based on outcome with dynamic content
        if (bossLevel == 1) {
            // Flameclaw (Level 1)
            enhancedStoryView.showBossBattleResult(success, bossLevel);

            // Record progress if successful
            if (success) {
                progressTracker.completeLevel("Beginner", bossLevel, 3);

                // After a successful completion of Level 1, transition to Level 2
                Timer transitionTimer = new Timer(5000, e -> {
                    transitionToLevel2();
                });
                transitionTimer.setRepeats(false);
                transitionTimer.start();
            }
        } else if (bossLevel == 2) {
            // IMPORTANT: For Toxitar specifically, use the specialized method
            System.out.println("DEBUG: Showing Level 2 boss battle result for Toxitar");

            // Make sure enhanced story view is visible
            enhancedStoryView.setVisible(true);

            // Force repaint before showing dialogue
            enhancedStoryView.revalidate();
            enhancedStoryView.repaint();

            // Short delay to ensure UI is updated
            Timer dialogueTimer = new Timer(500, e -> {
                enhancedStoryView.showLevel2BossBattleResult(success, selectedPotion);
            });
            dialogueTimer.setRepeats(false);
            dialogueTimer.start();

            // Record progress if successful
            if (success) {
                progressTracker.completeLevel("Intermediate", 1, 3);
            }
        }
    }
    
    /**
     * Start a specific level
     */
    public void startLevel(String difficulty, int level) {
        // Reset transition flag
        inLevelTransition = false;

        model.setCurrentLevel(level);
        model.setCurrentDifficulty(difficulty);

        // Set game state based on level configuration
        for (LevelConfig config : allLevels) {
            if (config.getDifficulty().equals(difficulty) && config.getLevelNumber() == level) {
                model.setCurrentState(config.getAlgorithmType());
                break;
            }
        }

        // Special handling for Level 2
        if (difficulty.equals("Intermediate") && level == 1) {
            // This is Level 2 in the game
            model.setGameLevel(2);
            // Reset TimSort visualization for Level 2
            timSortVisualization.resetAllPhases();
            timSortVisualization.setGameLevel(2);
            startLevel2FromSelection();
        }
        // For Level 1, start the story
        else if (difficulty.equals("Beginner") && level == 1) {
            model.setGameLevel(1);
            // Reset TimSort visualization for Level 1
            timSortVisualization.resetAllPhases();
            timSortVisualization.setGameLevel(1); 
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
    
    
    
    /**
    * Method to handle phase transitions with dynamic dialogue in GameController
    */
    public void onPhaseAdvance(int phase, String leftPotionType, String rightPotionType) {
        System.out.println("DEBUG: Phase advanced to " + phase);
        System.out.println("DEBUG: Left potion type: " + leftPotionType);
        System.out.println("DEBUG: Right potion type: " + rightPotionType);

        // Store potion types in the model for later use
        // This allows sharing this information between components
        if (model != null) {
            model.setLeftPotionType(leftPotionType);
            model.setRightPotionType(rightPotionType);
        }

        // We can use these when returning to story mode to show dynamic dialogue
    }
    
    
    
    
    
    /**
    * Start Level 2 with Toxitar as the boss
    */
    private void startLevel2() {
        // Set Level 2 in the model
        model.setGameLevel(2);
        model.setCurrentState(GameState.STORY_MODE);
        model.setCurrentLevel(1); // Reset to first phase

        // Reset TimSort visualization for Level 2
        timSortVisualization.resetAllPhases();
        timSortVisualization.setGameLevel(2);

        // Show Level 2 story introduction
        cardLayout.show(mainPanel, "enhancedStory");
        enhancedStoryView.startLevel2Story();
    }

    
    
    /**
    * Handle completion of Level 1 and transition to Level 2
    */
    public void onLevel1Complete() {
        // Update game level
        model.setGameLevel(2);

        // Show transition dialogue
        List<NarrativeSystem.DialogueEntry> transitionDialogues = 
            narrativeSystem.getDialogueSequence("level2_transition");
        enhancedStoryView.showTransitionDialogue(transitionDialogues, this::startLevel2);
    }
    
    
    /**
    * Transition from Level 1 to Level 2 after defeating Flameclaw
    */
    private void transitionToLevel2() {
        // Update game level in model
        model.setGameLevel(2);
        model.setCurrentLevel(1); // Reset to first phase

        // Show transition dialogue
        List<NarrativeSystem.DialogueEntry> transitionDialogues = 
            narrativeSystem.getDialogueSequence("level1to2_transition");

        if (transitionDialogues != null && !transitionDialogues.isEmpty()) {
            cardLayout.show(mainPanel, "enhancedStory");
            enhancedStoryView.showTransitionDialogue(transitionDialogues, () -> {
                startLevel2();
            });
        } else {
            // Fallback if dialogue is missing
            startLevel2();
        }
    }
    
    
    
    /**
    * Get the number of stars earned for a level
    */
    public int getStarsForLevel(String difficulty, int level) {
        return progressTracker.getStarsForLevel(difficulty, level);
    }
    
    
    
    /**
    * Start Level 2 directly from level selection
    */
    public void startLevel2FromSelection() {
        // Set Level 2 in the model
        model.setGameLevel(2);
        model.setCurrentState(GameState.STORY_MODE);
        model.setCurrentLevel(1); // Start at phase 1

        // Reset TimSort visualization for Level 2
        timSortVisualization.resetAllPhases();
        timSortVisualization.setGameLevel(2);

        // Show Level 2 story introduction
        cardLayout.show(mainPanel, "enhancedStory");
        enhancedStoryView.startLevel2Story();
    }
    
    // Add helper function to check if a level is completed
    public boolean isLevelCompleted(String difficulty, int level) {
        return progressTracker.isLevelCompleted(difficulty, level);
    }
    
    /**
    * Store potion types from visualization in the model
    * This ensures dialogue can access these values
    */
    public void storePotionTypes(String leftPotionType, String rightPotionType) {
        if (model != null) {
            model.setLeftPotionType(leftPotionType);
            model.setRightPotionType(rightPotionType);
            System.out.println("DEBUG: Stored potion types in model: " + leftPotionType + ", " + rightPotionType);
        }
    }
    
    
}



