package gameproject.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

import gameproject.model.NarrativeSystem;
import gameproject.controller.GameController;
import gameproject.util.ResourceManager;
import gameproject.util.GameConstants;

/**
 * DialogueManager handles the display and progression of dialogue
 * in the TimSort RPG game.
 */
public class DialogueManager extends JPanel {
    // References
    private NarrativeSystem narrativeSystem;
    private GameController controller;
    private ResourceManager resourceManager;
    
    // UI Components
    private JPanel dialoguePanel;
    private JLabel characterImageLabel;
    private JLabel characterNameLabel;
    private TransparentTextPane dialogueTextPane; // Changed from JLabel to TransparentTextPane
    private JButton nextButton;
    private JButton skipButton;
    
    // Current dialogue state
    private List<NarrativeSystem.DialogueEntry> currentDialogueSequence;
    private int currentDialogueIndex = 0;
    private boolean isDialoguePlaying = false;
    
    // Typewriter effect
    private Timer typewriterTimer;
    private String fullDialogueText;
    private int currentCharIndex = 0;
    private static final int TYPING_SPEED = 30; // milliseconds per character
    
    // Fixed dimensions with proper spacing to prevent cutoff
    private static final int DIALOGUE_WIDTH = 850;
    private static final int DIALOGUE_HEIGHT = 250;
    private static final int CHARACTER_IMAGE_SIZE = 200;
    private static final int BOTTOM_MARGIN = 120;
    
    /**
     * Constructor - Initialize the dialogue manager
     */
    public DialogueManager(GameController controller) {
        this.controller = controller;
        this.narrativeSystem = NarrativeSystem.getInstance();
        this.resourceManager = ResourceManager.getInstance();
        
        setLayout(null); // Use absolute positioning
        setOpaque(false);
        
        // Initialize UI
        createDialoguePanel();
    }
    
    /**
     * Create the dialogue panel with character portrait and text area
     */
    private void createDialoguePanel() {
        // Main dialogue panel with fixed width and semi-transparent background
        dialoguePanel = new JPanel();
        dialoguePanel.setLayout(new BorderLayout(10, 0));
        dialoguePanel.setBackground(new Color(0, 0, 0, 180)); // Lighter semi-transparent black
        dialoguePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Position dialoguePanel at the bottom of the screen with proper margins
        int x = (GameConstants.WINDOW_WIDTH - DIALOGUE_WIDTH) / 2;
        int y = GameConstants.WINDOW_HEIGHT - DIALOGUE_HEIGHT - BOTTOM_MARGIN;
        dialoguePanel.setBounds(x, y, DIALOGUE_WIDTH, DIALOGUE_HEIGHT);
        
        // Create a fixed-size panel for the character image with a consistent border on all sides
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4)); // Even padding on all sides
        
        // Character image with consistent border on all sides
        characterImageLabel = new JLabel();
        characterImageLabel.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
        characterImageLabel.setHorizontalAlignment(JLabel.CENTER);
        characterImageLabel.setVerticalAlignment(JLabel.CENTER);
        
        // Add the character image label to its panel with proper constraints
        imagePanel.add(characterImageLabel, BorderLayout.CENTER);
        
        // Character name and dialogue panel with transparent background
        JPanel textPanel = new JPanel(new BorderLayout(0, 10));
        textPanel.setOpaque(false); // Keep it transparent to show the dialogue panel background
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        // Character name with matching transparency
        characterNameLabel = new JLabel();
        characterNameLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        characterNameLabel.setForeground(Color.WHITE);
        characterNameLabel.setOpaque(false); // Ensure transparency
        
        // Use our custom TransparentTextPane instead of JLabel
        dialogueTextPane = new TransparentTextPane();
        dialogueTextPane.setPreferredSize(new Dimension(480, 150));
        
        // Add text components to the text panel
        textPanel.add(characterNameLabel, BorderLayout.NORTH);
        textPanel.add(dialogueTextPane, BorderLayout.CENTER);
        
        // Create button panel with improved spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);
        
        skipButton = new JButton("Skip All");
        skipButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        skipButton.setFocusPainted(false);
        skipButton.addActionListener(e -> skipDialogue());
        
        nextButton = new JButton("Next >");
        nextButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        nextButton.setFocusPainted(false);
        nextButton.addActionListener(e -> advanceDialogue());
        
        buttonPanel.add(skipButton);
        buttonPanel.add(nextButton);
        
        // Add components to the dialogue panel with proper constraints
        dialoguePanel.add(imagePanel, BorderLayout.WEST);
        dialoguePanel.add(textPanel, BorderLayout.CENTER);
        dialoguePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add dialogue panel to this panel
        add(dialoguePanel);
        
        // Set up typewriter timer
        typewriterTimer = new Timer(TYPING_SPEED, e -> {
            if (currentCharIndex < fullDialogueText.length()) {
                // Use the TransparentTextPane's setHtmlText method
                dialogueTextPane.setHtmlText(fullDialogueText.substring(0, currentCharIndex + 1));
                currentCharIndex++;
            } else {
                typewriterTimer.stop();
            }
        });
        
        // Add click listener to dialogue area to speed up text
        dialogueTextPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (typewriterTimer.isRunning()) {
                    // Show all text immediately when clicked
                    dialogueTextPane.setHtmlText(fullDialogueText);
                    currentCharIndex = fullDialogueText.length();
                    typewriterTimer.stop();
                } else {
                    // Advance dialogue when clicked after text is fully shown
                    advanceDialogue();
                }
            }
        });
        
        // Initially hide the dialogue panel
        dialoguePanel.setVisible(false);
    }
    
    /**
     * Start displaying a dialogue sequence
     */
    public void startDialogue(List<NarrativeSystem.DialogueEntry> dialogueSequence) {
        if (dialogueSequence == null || dialogueSequence.isEmpty()) {
            return;
        }
        
        this.currentDialogueSequence = dialogueSequence;
        this.currentDialogueIndex = 0;
        this.isDialoguePlaying = true;
        
        // Show the dialogue panel
        dialoguePanel.setVisible(true);
        setVisible(true);
        
        // Display first dialogue entry
        displayDialogueEntry(currentDialogueSequence.get(currentDialogueIndex));
    }
    
    /**
     * Display a single dialogue entry with character portrait and text
     */
    private void displayDialogueEntry(NarrativeSystem.DialogueEntry entry) {
        // Get character info
        NarrativeSystem.StoryCharacter character = entry.getCharacterObject();
        String characterName = character.getName();
        String dialogue = entry.getText();
        String emotion = entry.getEmotion();
        
        // Load character portrait
        String portraitPath = character.getImagePath();
        if (emotion != null && !emotion.isEmpty()) {
            // Append emotion to get specific expression image
            portraitPath = portraitPath.replace(".png", "_" + emotion + ".png");
        }
        
        ImageIcon portrait = resourceManager.getImage(portraitPath);
        if (portrait != null) {
            // Create a perfectly square image with proper padding on all sides
            int imageSize = CHARACTER_IMAGE_SIZE - 12; // Account for the border and padding
            Image scaledImage = portrait.getImage().getScaledInstance(
                imageSize,
                imageSize,
                Image.SCALE_SMOOTH
            );
            characterImageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            // Fallback to default silhouette if image not found
            ImageIcon defaultIcon = resourceManager.getImage("/gameproject/resources/characters/default.png");
            if (defaultIcon != null) {
                Image scaledImage = defaultIcon.getImage().getScaledInstance(
                    CHARACTER_IMAGE_SIZE - 12, // Same as portrait image
                    CHARACTER_IMAGE_SIZE - 12,
                    Image.SCALE_SMOOTH
                );
                characterImageLabel.setIcon(new ImageIcon(scaledImage));
            }
        }
        
        // Set character name
        characterNameLabel.setText(characterName);
        
        // Start typewriter effect for dialogue text
        fullDialogueText = dialogue;
        currentCharIndex = 0;
        dialogueTextPane.setHtmlText(""); // Clear previous text
        typewriterTimer.setInitialDelay(0); // Ensure immediate start
        typewriterTimer.start();
        
        // Update UI
        revalidate();
        repaint();
    }
    
    /**
     * Advance to the next dialogue entry
     */
    private void advanceDialogue() {
        // Stop animation
        typewriterTimer.stop();
        
        // Move to next dialogue
        currentDialogueIndex++;
        
        // Check if we've reached the end of the sequence
        if (currentDialogueIndex >= currentDialogueSequence.size()) {
            endDialogueSequence();
            return;
        }
        
        // Display next dialogue entry
        displayDialogueEntry(currentDialogueSequence.get(currentDialogueIndex));
    }
    
    /**
     * Skip the entire dialogue sequence
     */
    private void skipDialogue() {
        // Stop animation
        typewriterTimer.stop();
        
        // End the dialogue sequence
        endDialogueSequence();
    }
    
    /**
     * End the current dialogue sequence and signal the narrative system to advance
     */
    private void endDialogueSequence() {
        isDialoguePlaying = false;
        dialoguePanel.setVisible(false);
        setVisible(false);
        
        // Signal the narrative system to advance the story
        narrativeSystem.advanceStory();
        
        // Notify controller that dialogue has ended
        controller.onDialogueSequenceEnded();
    }
    
    /**
     * Check if a dialogue is currently playing
     */
    public boolean isDialoguePlaying() {
        return isDialoguePlaying;
    }
    
    /**
     * Paint the component without any additional background
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // No overlay - removed to prevent text rendering issues with the background
    }
}