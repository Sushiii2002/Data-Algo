package gameproject.view;

import gameproject.controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Main menu view for the game
 */
public class MainMenuView extends JPanel {
    private GameController controller;

    public MainMenuView(GameController controller) {
        this.controller = controller;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = createTitlePanel();
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        
        // Add components to main panel
        add(titlePanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel("SmartSortStory");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Learn Sorting Algorithms Through Interactive Storytelling");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(Box.createVerticalGlue());
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        titlePanel.add(subtitleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        return titlePanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        
        JButton startButton = createMenuButton("Start Game");
        JButton levelsButton = createMenuButton("Select Level");
        JButton helpButton = createMenuButton("How to Play");
        JButton exitButton = createMenuButton("Exit");
        
        startButton.addActionListener(e -> controller.showStory());
        levelsButton.addActionListener(e -> controller.showLevelSelection());
        helpButton.addActionListener(e -> controller.showHelp());
        exitButton.addActionListener(e -> controller.exitGame());
        
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(startButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(levelsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(helpButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createVerticalGlue());
        
        return buttonPanel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setMaximumSize(new Dimension(200, 50));
        button.setPreferredSize(new Dimension(200, 50));
        return button;
    }
}