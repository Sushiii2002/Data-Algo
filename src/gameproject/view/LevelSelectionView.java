package gameproject.view;

import gameproject.controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Level selection view for the game
 */
public class LevelSelectionView extends JPanel {
    private GameController controller;

    public LevelSelectionView(GameController controller) {
        this.controller = controller;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Select Level", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // Difficulty tabs
        JTabbedPane difficultyTabs = new JTabbedPane();

        // Beginner levels panel
        JPanel beginnerPanel = createLevelGrid("Beginner", 5);
        difficultyTabs.addTab("Beginner", beginnerPanel);

        // Intermediate levels panel
        JPanel intermediatePanel = createLevelGrid("Intermediate", 5);
        difficultyTabs.addTab("Intermediate", intermediatePanel);

        // Advanced levels panel
        JPanel advancedPanel = createLevelGrid("Advanced", 5);
        difficultyTabs.addTab("Advanced", advancedPanel);

        add(difficultyTabs, BorderLayout.CENTER);

        // Back button
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> controller.showMainMenu());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Create a grid of level buttons for a specific difficulty
     */
    private JPanel createLevelGrid(String difficulty, int levelCount) {
        JPanel panel = new JPanel(new GridLayout(0, 5, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        for (int i = 1; i <= levelCount; i++) {
            final int level = i;
            JButton levelButton = new JButton("Level " + i);
            levelButton.addActionListener(e -> controller.startLevel(difficulty, level));
            panel.add(levelButton);
        }

        return panel;
    }
}