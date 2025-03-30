package gameproject.view;

import gameproject.controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * View for the story mode screen
 */
public class StoryView extends JPanel {
    private GameController controller;
    
    /**
     * Constructor - Initialize the story view
     */
    public StoryView(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
       
        // Story title
        JLabel titleLabel = new JLabel("The Tale of SmartSortStory", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);
       
        // Story content
        JTextArea storyText = new JTextArea();
        storyText.setText("Once upon a time in the digital realm of Algorithmia, there was chaos in the " +
                "Kingdom of Data. The king needed help to organize the royal archives...\n\n" +
                "You, a young sorting apprentice, have been chosen to restore order using the " +
                "ancient techniques of Insertion Sort and Merge Sort, ultimately mastering " +
                "the legendary Tim Sort algorithm.\n\n" +
                "Your journey begins now. Are you ready to bring order to chaos?");
        storyText.setWrapStyleWord(true);
        storyText.setLineWrap(true);
        storyText.setEditable(false);
        storyText.setFont(new Font("Arial", Font.PLAIN, 16));
        storyText.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
       
        JScrollPane scrollPane = new JScrollPane(storyText);
        add(scrollPane, BorderLayout.CENTER);
       
        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton skipButton = new JButton("Skip Story");
        JButton continueButton = new JButton("Continue to Game");
       
        skipButton.addActionListener(e -> controller.skipStory());
        continueButton.addActionListener(e -> controller.startInsertionSortChallenge());
       
        buttonPanel.add(skipButton);
        buttonPanel.add(continueButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}