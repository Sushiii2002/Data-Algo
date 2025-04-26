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
    private JLabel dialogueTextLabel;
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
    
    // Fixed dimensions
    private static final int DIALOGUE_WIDTH = 850;
    private static final int DIALOGUE_HEIGHT = 250;
    private static final int CHARACTER_IMAGE_SIZE = 250;
    private static final int BOTTOM_MARGIN = 120; // Increased bottom margin to move dialogue box higher
    
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
        // Main dialogue panel with fixed width
        dialoguePanel = new JPanel();
        dialoguePanel.setLayout(new BorderLayout());
        dialoguePanel.setBackground(new Color(0, 0, 0, 200)); // Semi-transparent black
        dialoguePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Position dialoguePanel at the bottom of the screen with proper margins
        // MOVED UP: Increased bottom margin from 80 to 120 pixels
        int x = (GameConstants.WINDOW_WIDTH - DIALOGUE_WIDTH) / 2;
        int y = GameConstants.WINDOW_HEIGHT - DIALOGUE_HEIGHT - BOTTOM_MARGIN;
        dialoguePanel.setBounds(x, y, DIALOGUE_WIDTH, DIALOGUE_HEIGHT);
        
        // FIXED: Use a fixed-size panel with absolute layout for the character image
        JPanel imagePanel = new JPanel(null);
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE));
        imagePanel.setMinimumSize(new Dimension(CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE));
        imagePanel.setMaximumSize(new Dimension(CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE));
        
        // Character image with explicit size constraints
        characterImageLabel = new JLabel();
        characterImageLabel.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
        characterImageLabel.setBounds(0, 0, CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE);
        characterImageLabel.setPreferredSize(new Dimension(CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE));
        characterImageLabel.setMinimumSize(new Dimension(CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE));
        characterImageLabel.setMaximumSize(new Dimension(CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE));
        
        // Add the character image label to its panel
        imagePanel.add(characterImageLabel);
        
        // Character name and dialogue panel
        JPanel textPanel = new JPanel(new BorderLayout(0, 10));
        textPanel.setOpaque(false);
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        // Character name
        characterNameLabel = new JLabel();
        characterNameLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        characterNameLabel.setForeground(Color.WHITE);
        
        // Dialogue text
        dialogueTextLabel = new JLabel();
        dialogueTextLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        dialogueTextLabel.setForeground(Color.WHITE);
        dialogueTextLabel.setVerticalAlignment(JLabel.TOP);
        
        // Add text components to the text panel
        textPanel.add(characterNameLabel, BorderLayout.NORTH);
        textPanel.add(dialogueTextLabel, BorderLayout.CENTER);
        
        // Create button panel - improved spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5)); // Added vertical padding
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
        
        // Add components to the dialogue panel
        dialoguePanel.add(imagePanel, BorderLayout.WEST);
        dialoguePanel.add(textPanel, BorderLayout.CENTER);
        dialoguePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add dialogue panel to this panel
        add(dialoguePanel);
        
        // Set up typewriter timer
        typewriterTimer = new Timer(TYPING_SPEED, e -> {
            if (currentCharIndex < fullDialogueText.length()) {
                dialogueTextLabel.setText("<html><div style='width:500px'>" + fullDialogueText.substring(0, currentCharIndex + 1) + "</div></html>");
                currentCharIndex++;
            } else {
                typewriterTimer.stop();
            }
        });
        
        // Add click listener to dialogue area to speed up text
        dialogueTextLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (typewriterTimer.isRunning()) {
                    // Show all text immediately when clicked
                    dialogueTextLabel.setText("<html><div style='width:500px'>" + fullDialogueText + "</div></html>");
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
            // Create a perfectly square image at the exact size
            Image scaledImage = portrait.getImage().getScaledInstance(
                CHARACTER_IMAGE_SIZE - 4, // Account for the border width
                CHARACTER_IMAGE_SIZE - 4, // Account for the border width
                Image.SCALE_SMOOTH
            );
            characterImageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            // Fallback to default silhouette if image not found
            ImageIcon defaultIcon = resourceManager.getImage("/gameproject/resources/characters/default.png");
            if (defaultIcon != null) {
                Image scaledImage = defaultIcon.getImage().getScaledInstance(
                    CHARACTER_IMAGE_SIZE - 4,
                    CHARACTER_IMAGE_SIZE - 4,
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
        dialogueTextLabel.setText("");
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
     * Paint the component with NO overlay background
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // No overlay - removed to prevent text rendering issues with the background
    }
}