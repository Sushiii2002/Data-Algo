package gameproject.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import gameproject.model.NarrativeSystem;
import gameproject.controller.GameController;
import gameproject.util.ResourceManager;
import gameproject.util.GameConstants;

/**
 * DialogueManager handles the display and progression of dialogue
 * in the TimSort RPG game.
 */
public class DialogueManager extends JPanel {
    // Logger for error handling
    private static final Logger LOGGER = Logger.getLogger(DialogueManager.class.getName());
    
    // References
    private NarrativeSystem narrativeSystem;
    private GameController controller;
    private ResourceManager resourceManager;
    
    // Simple UI components - completely separate with no nesting
    private JPanel dialoguePanel;
    private PortraitPanel portraitPanel; // Custom panel for portrait with border
    private JLabel characterNameLabel;
    private JLabel dialogueTextLabel;
    private JButton nextButton;
    private JButton skipButton;
    
    // Current character role for border styling
    private String currentCharacterRole = "default";
    private String currentCharacterName = "";
    
    // Portrait cache
    private Map<String, ImageIcon> portraitCache = new HashMap<>();
    private ImageIcon placeholderSilhouette = null;
    
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
    private static final int CHARACTER_IMAGE_SIZE = 196; // Exact requirement
    private static final int BOTTOM_MARGIN = 120;
    
    // Font sizes - increased base size
    private static final float DIALOGUE_FONT_SIZE = 26.0f; // Increased from 22 to 26
    private static final float CHARACTER_NAME_FONT_SIZE = 28.0f; // Increased from 24 to 28
    private static final float CHARACTER_NAME_BOSS_FONT_SIZE = 30.0f; // Increased from 26 to 30
    
    /**
     * Custom panel for drawing portraits with themed borders
     */
    private class PortraitPanel extends JPanel {
        private ImageIcon portrait;
        
        public PortraitPanel() {
            setOpaque(false);
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE));
        }
        
        public void setPortrait(ImageIcon portrait) {
            this.portrait = portrait;
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw the portrait if available
            if (portrait != null) {
                // Get the image from the icon
                Image img = portrait.getImage();
                int imgWidth = img.getWidth(this);
                int imgHeight = img.getHeight(this);
                
                // Skip if image hasn't loaded
                if (imgWidth > 0 && imgHeight > 0) {
                    // Calculate scaling to fit within 90% of the area
                    double targetSize = Math.min(getWidth(), getHeight()) * 0.9;
                    double scale = Math.min(targetSize / imgWidth, targetSize / imgHeight);
                    
                    int scaledWidth = (int)(imgWidth * scale);
                    int scaledHeight = (int)(imgHeight * scale);
                    
                    // Calculate centering position
                    int x = (getWidth() - scaledWidth) / 2;
                    int y = (getHeight() - scaledHeight) / 2;
                    
                    // Use nearest-neighbor scaling for pixel art
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                       RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    
                    g2d.drawImage(img, x, y, scaledWidth, scaledHeight, this);
                }
            }
            
            // Draw themed border based on character
            drawCharacterBorder(g2d);
        }
        
        /**
         * Draw a themed border based on character role
         */
        private void drawCharacterBorder(Graphics2D g2d) {
            // Define border colors based on character role
            Color primaryColor;
            Color secondaryColor;
            Color accentColor;
            
            switch (currentCharacterRole) {
                case "Mentor": // Gold/bronze for Master Ordin
                    primaryColor = new Color(205, 170, 40);
                    secondaryColor = new Color(160, 130, 30);
                    accentColor = new Color(230, 200, 80);
                    break;
                case "Hero": // Blue/silver for Tima
                    primaryColor = new Color(80, 120, 200);
                    secondaryColor = new Color(60, 90, 150);
                    accentColor = new Color(150, 180, 225);
                    break;
                case "Helper": 
                    if (currentCharacterName.contains("Runa")) {
                        // Nature theme for Runa
                        primaryColor = new Color(70, 160, 50);
                        secondaryColor = new Color(50, 120, 35);
                        accentColor = new Color(120, 200, 80);
                    } else if (currentCharacterName.contains("Balz")) {
                        // Merchant theme
                        primaryColor = new Color(160, 100, 40);
                        secondaryColor = new Color(120, 70, 20);
                        accentColor = new Color(200, 150, 50);
                    } else {
                        // Default helper
                        primaryColor = new Color(120, 120, 180);
                        secondaryColor = new Color(90, 90, 150);
                        accentColor = new Color(150, 150, 220);
                    }
                    break;
                case "Boss": // Red/black for bosses
                    primaryColor = new Color(180, 30, 30);
                    secondaryColor = new Color(90, 15, 15);
                    accentColor = new Color(255, 60, 60);
                    break;
                default: // Gray/silver default
                    primaryColor = new Color(150, 150, 150);
                    secondaryColor = new Color(100, 100, 100);
                    accentColor = new Color(200, 200, 200);
                    break;
            }
            
            // Save original stroke
            Stroke originalStroke = g2d.getStroke();
            
            // Draw 3px themed border
            g2d.setStroke(new BasicStroke(3f));
            g2d.setColor(primaryColor);
            g2d.drawRect(2, 2, getWidth() - 5, getHeight() - 5);
            
            // Add 1px accent highlight
            g2d.setStroke(new BasicStroke(1f));
            g2d.setColor(accentColor);
            g2d.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            
            // Draw corner accents
            int cornerSize = 10;
            g2d.setColor(secondaryColor);
            g2d.fillRect(1, 1, cornerSize, cornerSize); // Top left
            g2d.fillRect(getWidth() - cornerSize - 2, 1, cornerSize, cornerSize); // Top right
            g2d.fillRect(1, getHeight() - cornerSize - 2, cornerSize, cornerSize); // Bottom left
            g2d.fillRect(getWidth() - cornerSize - 2, getHeight() - cornerSize - 2, cornerSize, cornerSize); // Bottom right
            
            // Add 1px highlight to corners
            g2d.setColor(accentColor);
            g2d.drawRect(1, 1, cornerSize, cornerSize); // Top left
            g2d.drawRect(getWidth() - cornerSize - 2, 1, cornerSize, cornerSize); // Top right
            g2d.drawRect(1, getHeight() - cornerSize - 2, cornerSize, cornerSize); // Bottom left
            g2d.drawRect(getWidth() - cornerSize - 2, getHeight() - cornerSize - 2, cornerSize, cornerSize); // Bottom right
            
            // Restore original stroke
            g2d.setStroke(originalStroke);
        }
    }
    
    /**
     * Constructor - Initialize the dialogue manager
     */
    public DialogueManager(GameController controller) {
        this.controller = controller;
        this.narrativeSystem = NarrativeSystem.getInstance();
        this.resourceManager = ResourceManager.getInstance();
        
        setLayout(null); // Use absolute positioning
        setOpaque(false);
        
        // Load placeholder silhouette
        loadPlaceholderSilhouette();
        
        // Initialize UI
        createSimplifiedUI();
    }
    
    /**
     * Load the placeholder silhouette image
     */
    private void loadPlaceholderSilhouette() {
        placeholderSilhouette = resourceManager.getImage("/gameproject/resources/characters/silhouette.png");
        
        // If not found, create a default silhouette
        if (placeholderSilhouette == null) {
            BufferedImage silhouette = new BufferedImage(CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE, 
                                                        BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = silhouette.createGraphics();
            
            // Fill with dark gray
            g2d.setColor(new Color(60, 60, 60));
            
            // Create a simple person silhouette shape
            int headSize = CHARACTER_IMAGE_SIZE / 3;
            int bodyWidth = CHARACTER_IMAGE_SIZE / 2;
            int bodyHeight = CHARACTER_IMAGE_SIZE / 2;
            
            // Draw head
            g2d.fillOval((CHARACTER_IMAGE_SIZE - headSize) / 2, 
                         CHARACTER_IMAGE_SIZE / 6, headSize, headSize);
            
            // Draw body
            g2d.fillRect((CHARACTER_IMAGE_SIZE - bodyWidth) / 2, 
                         CHARACTER_IMAGE_SIZE / 6 + headSize, 
                         bodyWidth, bodyHeight);
            
            g2d.dispose();
            
            placeholderSilhouette = new ImageIcon(silhouette);
            LOGGER.info("Created default silhouette as placeholder");
        }
    }
    
    /**
     * Create a completely simplified UI with no overlays but proper text wrapping
     */
    private void createSimplifiedUI() {
        // Calculate dialogue panel position
        int x = (GameConstants.WINDOW_WIDTH - DIALOGUE_WIDTH) / 2;
        int y = GameConstants.WINDOW_HEIGHT - DIALOGUE_HEIGHT - BOTTOM_MARGIN;
        
        // Main container panel - NO BACKGROUND
        dialoguePanel = new JPanel(null); // Use null layout for absolute positioning
        dialoguePanel.setBounds(x, y, DIALOGUE_WIDTH, DIALOGUE_HEIGHT);
        dialoguePanel.setOpaque(false); // No background
        
        // Portrait panel with custom rendering and border
        portraitPanel = new PortraitPanel();
        portraitPanel.setBounds(10, 10, CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE);
        dialoguePanel.add(portraitPanel);
        
        // Character name label
        characterNameLabel = new JLabel();
        characterNameLabel.setFont(new Font("SansSerif", Font.BOLD, (int)CHARACTER_NAME_FONT_SIZE));
        characterNameLabel.setForeground(Color.WHITE);
        characterNameLabel.setBounds(CHARACTER_IMAGE_SIZE + 25, 10, 400, 35); // Increased height slightly for bigger font
        characterNameLabel.setOpaque(false); // No background
        dialoguePanel.add(characterNameLabel);
        
        // Dialogue text label - using HTML for multi-line text
        dialogueTextLabel = new JLabel();
        dialogueTextLabel.setFont(new Font("SansSerif", Font.PLAIN, (int)DIALOGUE_FONT_SIZE));
        dialogueTextLabel.setForeground(Color.WHITE);
        dialogueTextLabel.setVerticalAlignment(JLabel.TOP);
        
        // CRITICAL FIX: Reduce the width significantly to prevent text from being cut off on the right edge
        // And increase the height to accommodate the larger font size
        dialogueTextLabel.setBounds(
            CHARACTER_IMAGE_SIZE + 25,       // Start after portrait with padding
            45,                              // Top position
            DIALOGUE_WIDTH - CHARACTER_IMAGE_SIZE - 70,  // Reduced width by 35px to prevent cutoff
            160                              // Increased height to accommodate larger font
        );
        dialogueTextLabel.setOpaque(false); // No background
        dialoguePanel.add(dialogueTextLabel);
        
        // Skip button
        skipButton = new JButton("Skip All");
        skipButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        skipButton.setFocusPainted(false);
        skipButton.setBounds(DIALOGUE_WIDTH - 180, DIALOGUE_HEIGHT - 40, 90, 30);
        skipButton.addActionListener(e -> skipDialogue());
        dialoguePanel.add(skipButton);
        
        // Next button
        nextButton = new JButton("Next >");
        nextButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        nextButton.setFocusPainted(false);
        nextButton.setBounds(DIALOGUE_WIDTH - 80, DIALOGUE_HEIGHT - 40, 90, 30);
        nextButton.addActionListener(e -> advanceDialogue());
        dialoguePanel.add(nextButton);
        
        // Add dialogue panel to this panel
        add(dialoguePanel);
        
        // Set up typewriter timer
        typewriterTimer = new Timer(TYPING_SPEED, e -> {
            if (currentCharIndex < fullDialogueText.length()) {
                // Display text up to current character
                updateDialogueText(fullDialogueText.substring(0, currentCharIndex + 1));
                currentCharIndex++;
            } else {
                typewriterTimer.stop();
            }
        });
        
        // Add click listener to dialogue text to skip typing or advance
        dialogueTextLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (typewriterTimer.isRunning()) {
                    // Show all text immediately when clicked
                    updateDialogueText(fullDialogueText);
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

        // Store character role and name for border styling
        currentCharacterRole = character.getRole();
        currentCharacterName = characterName;

        // Load character portrait
        loadCharacterPortrait(characterName, emotion);

        // Set character name with appropriate styling based on character type
        if (character.getRole().equals("Boss")) {
            characterNameLabel.setForeground(Color.RED);
            characterNameLabel.setFont(new Font("SansSerif", Font.BOLD, (int)CHARACTER_NAME_BOSS_FONT_SIZE));
        } else if (character.getRole().equals("Mentor")) {
            characterNameLabel.setForeground(new Color(150, 150, 255));
            characterNameLabel.setFont(new Font("SansSerif", Font.BOLD, (int)CHARACTER_NAME_FONT_SIZE));
        } else {
            characterNameLabel.setForeground(Color.WHITE);
            characterNameLabel.setFont(new Font("SansSerif", Font.BOLD, (int)CHARACTER_NAME_FONT_SIZE));
        }
        characterNameLabel.setText(characterName);

        // Start typewriter effect for dialogue text
        fullDialogueText = dialogue;
        currentCharIndex = 0;
        updateDialogueText(""); // Clear previous text
        typewriterTimer.start();
        
        // Force repaint to ensure border is updated
        portraitPanel.repaint();
    }
    
    /**
     * Update dialogue text with proper HTML formatting for better text wrapping
     */
    private void updateDialogueText(String text) {
        if (text == null || text.isEmpty()) {
            dialogueTextLabel.setText("");
            return;
        }
        
        // Calculate the correct width for text wrapping
        // This is critical to prevent text cutoff
        int textWidth = dialogueTextLabel.getWidth() - 150; // Increased margin from 10 to 40 for greater safety
        
        // Create HTML with specific width to force proper text wrapping
        StringBuilder html = new StringBuilder();
        html.append("<html><div style='width: ").append(textWidth).append("px;'>");
        
        // Replace newlines with HTML breaks
        html.append(text.replace("\n", "<br>"));
        
        html.append("</div></html>");
        
        // Set the wrapped text
        dialogueTextLabel.setText(html.toString());
    }
    
    /**
     * Load a character portrait with the specified emotion
     */
    private void loadCharacterPortrait(String characterName, String emotion) {
        // Format character name to match file naming
        String formattedName = characterName.toLowerCase().replace(" ", "_");
        
        // Create portrait file path
        String portraitPath;
        if (emotion != null && !emotion.isEmpty()) {
            portraitPath = "/gameproject/resources/characters/" + formattedName + "_" + emotion + ".png";
        } else {
            portraitPath = "/gameproject/resources/characters/" + formattedName + ".png";
        }
        
        // Check if portrait is already cached
        String cacheKey = portraitPath;
        ImageIcon portrait = portraitCache.get(cacheKey);
        
        // If not in cache, load it
        if (portrait == null) {
            portrait = resourceManager.getImage(portraitPath);
            
            // If emotion-specific portrait not found, try default portrait
            if (portrait == null && emotion != null && !emotion.isEmpty()) {
                LOGGER.log(Level.INFO, "Emotion portrait not found: {0}. Falling back to default.", portraitPath);
                portraitPath = "/gameproject/resources/characters/" + formattedName + ".png";
                portrait = resourceManager.getImage(portraitPath);
            }
            
            // If still not found, use silhouette placeholder
            if (portrait == null) {
                LOGGER.log(Level.WARNING, "Character portrait not found: {0}. Using placeholder.", portraitPath);
                portrait = placeholderSilhouette;
            } else {
                // Cache the loaded portrait
                portraitCache.put(cacheKey, portrait);
            }
        }
        
        // Set the portrait in panel
        portraitPanel.setPortrait(portrait);
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
        
        // Clear components
        portraitPanel.setPortrait(null);
        characterNameLabel.setText("");
        dialogueTextLabel.setText("");
        
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
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // No additional background painting
    }
    
}