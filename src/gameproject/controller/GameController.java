package gameproject.controller;

import gameproject.model.GameModel;
import gameproject.model.GameState;
import gameproject.view.MainMenuView;
import gameproject.view.LevelSelectionView;
import gameproject.view.StoryView;
import gameproject.view.GameView;

import javax.swing.*;
import java.awt.*;

/**
 * Controls the game flow and interactions between model and views
 */
public class GameController {
    private static final String GAME_TITLE = "SmartSortStory";
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 600;

    private GameModel model;
    private JFrame mainFrame;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    private MainMenuView mainMenuView;
    private LevelSelectionView levelSelectionView;
    private StoryView storyView;
    private GameView gameView;

    public GameController() {
        // Initialize model
        model = new GameModel();

        // Set up the main frame
        initializeFrame();

        // Initialize views
        initializeViews();

        // Set initial state
        showMainMenu();
    }

    private void initializeFrame() {
        mainFrame = new JFrame(GAME_TITLE);
        mainFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        // Set up main panel with card layout
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        mainFrame.add(mainPanel);
    }

    private void initializeViews() {
        // Create views
        mainMenuView = new MainMenuView(this);
        levelSelectionView = new LevelSelectionView(this);
        storyView = new StoryView(this);
        gameView = new GameView(this);

        // Add views to main panel
        mainPanel.add(mainMenuView, "mainMenu");
        mainPanel.add(levelSelectionView, "levelSelection");
        mainPanel.add(storyView, "story");
        mainPanel.add(gameView, "game");
    }

    // Navigation methods
    public void showMainMenu() {
        model.setCurrentState(GameState.MAIN_MENU);
        cardLayout.show(mainPanel, "mainMenu");
    }

    public void showLevelSelection() {
        model.setCurrentState(GameState.LEVEL_SELECTION);
        cardLayout.show(mainPanel, "levelSelection");
    }

    public void showStory() {
        model.setCurrentState(GameState.STORY_MODE);
        cardLayout.show(mainPanel, "story");
    }

    public void startLevel(String difficulty, int level) {
        model.setCurrentDifficulty(difficulty);
        model.setCurrentLevel(level);
        model.setCurrentState(GameState.INSERTION_SORT_CHALLENGE);
        
        // Update game view with current level info
        gameView.updateLevelInfo(difficulty, level);
        cardLayout.show(mainPanel, "game");
    }

    public void showHelp() {
        JOptionPane.showMessageDialog(mainFrame,
            "SmartSortStory is an interactive game to learn sorting algorithms.\n\n" +
            "- Drag and drop elements to sort them (Insertion Sort)\n" +
            "- Merge sorted runs by selecting correct pairs (Merge Sort)\n" +
            "- Progress through levels from Beginner to Advanced\n" +
            "- Follow the story to complete the game",
            "How to Play", JOptionPane.INFORMATION_MESSAGE);
    }

    public void exitGame() {
        System.exit(0);
    }

    public void displayFrame() {
        mainFrame.setVisible(true);
    }

    // Getter for model
    public GameModel getModel() {
        return model;
    }
}