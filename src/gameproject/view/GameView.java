package gameproject.view;

import gameproject.controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Main game view for sorting challenges
 */
public class GameView extends JPanel {
    private GameController controller;
    private JLabel levelLabel;

    public GameView(GameController controller) {
        this.controller = controller;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Top panel with level info
        JPanel topPanel = createTopPanel();

        // Main game area
        JPanel gameArea = createGameArea();

        // Bottom panel with action buttons
        JPanel bottomPanel = createBottomPanel();

        // Add panels to game view
        add(topPanel, BorderLayout.NORTH);
        add(gameArea, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        levelLabel = new JLabel("Level 1 - Beginner");
        levelLabel.setFont(new Font("Arial", Font.BOLD, 18));
        levelLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));

        JPanel controlPanel = new JPanel();
        JButton hintButton = new JButton("Hint");
        JButton restartButton = new JButton("Restart");
        JButton menuButton = new JButton("Main Menu");

        menuButton.addActionListener(e -> controller.showMainMenu());

        controlPanel.add(hintButton);
        controlPanel.add(restartButton);
        controlPanel.add(menuButton);

        topPanel.add(levelLabel, BorderLayout.WEST);
        topPanel.add(controlPanel, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createGameArea() {
        JPanel gameArea = new JPanel();
        gameArea.setLayout(new BoxLayout(gameArea, BoxLayout.Y_AXIS));
        gameArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Instructions
        JLabel instructionsLabel = new JLabel("Drag and drop the elements to sort them:");
        instructionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        instructionsLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Sorting visualization area
        JPanel sortingArea = new JPanel();
        sortingArea.setBackground(Color.LIGHT_GRAY);
        sortingArea.setPreferredSize(new Dimension(800, 300));
        sortingArea.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        sortingArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        gameArea.add(instructionsLabel);
        gameArea.add(Box.createRigidArea(new Dimension(0, 20)));
        gameArea.add(sortingArea);

        return gameArea;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        JButton checkButton = new JButton("Check Solution");
        JButton nextButton = new JButton("Next Step");

        nextButton.addActionListener(e -> {
            // Placeholder for next step logic
            JOptionPane.showMessageDialog(this,
                    "Congratulations! You've completed this step.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        bottomPanel.add(checkButton);
        bottomPanel.add(nextButton);

        return bottomPanel;
    }

    /**
     * Update the level information displayed
     */
    public void updateLevelInfo(String difficulty, int level) {
        levelLabel.setText("Level " + level + " - " + difficulty);
    }
}