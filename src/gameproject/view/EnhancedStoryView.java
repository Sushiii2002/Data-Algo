package gameproject.view;

import gameproject.controller.GameController;
import gameproject.model.NarrativeSystem;
import gameproject.model.GameState;
import gameproject.util.ResourceManager;
import gameproject.util.GameConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.awt.image.BufferedImage;

/**
 * Enhanced story view that integrates with the NarrativeSystem
 * and displays story elements with animations and transitions.
 */
public class EnhancedStoryView extends JPanel {
    private GameController controller;
    private NarrativeSystem narrativeSystem;
    private ResourceManager resourceManager;
    private DialogueManager dialogueManager;
    
    // Background elements
    private ImageIcon backgroundImage;
    private JLabel titleLabel;
    private JPanel storyContentPanel;
    
    // Phase indicators
    private JPanel phaseIndicatorsPanel;
    private JLabel[] phaseLabels;
    private String[] phaseNames = {"The Eye of Pattern", "The Hand of Balance", "The Mind of Unity"};
    
    // Animation elements
    private Timer fadeInTimer;
    private Timer fadeOutTimer;
    private float alphaLevel = 0.0f;
    private boolean isFadingIn = false;
    private boolean isFadingOut = false;
    
    // Current phase tracking
    private int currentPhase = -1; // -1 indicates story intro
    
    /**
     * Constructor - Initialize the enhanced story view
     */
    public EnhancedStoryView(GameController controller) {
        this.controller = controller;
        this.narrativeSystem = NarrativeSystem.getInstance();
        this.resourceManager = ResourceManager.getInstance();
        
        // Use null layout for precise component positioning
        setLayout(null);
        
        // Load resources
        loadResources();
        
        // Initialize UI components
        initializeUI();
        
        // Initialize dialogue manager
        dialogueManager = new DialogueManager(controller);
        dialogueManager.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        add(dialogueManager);
        
        // Set up animation timers
        setupAnimationTimers();
    }
    
    /**
     * Load all required resources
     */
    private void loadResources() {
        // Load background image
        backgroundImage = resourceManager.getImage("/gameproject/resources/story_bg.png");
        
        // If background image is not found, create a fallback gradient
        if (backgroundImage == null) {
            // Create a fallback gradient background
            int width = GameConstants.WINDOW_WIDTH;
            int height = GameConstants.WINDOW_HEIGHT;
            
            BufferedImage fallbackImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = fallbackImage.createGraphics();
            
            // Create gradient from dark blue to lighter blue
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(20, 30, 60),
                0, height, new Color(50, 70, 120)
            );
            
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, width, height);
            g2d.dispose();
            
            backgroundImage = new ImageIcon(fallbackImage);
        }
    }
    
    /**
     * Initialize all UI components
     */
    private void initializeUI() {
        // Title label (initially invisible)
        titleLabel = new JLabel("The Tale of the Harmony of Order", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, 100, GameConstants.WINDOW_WIDTH, 50);
        titleLabel.setVisible(false);
        add(titleLabel);
        
        // Story content panel (for scrolling text)
        storyContentPanel = new JPanel();
        storyContentPanel.setLayout(new BorderLayout());
        storyContentPanel.setBounds(
            GameConstants.WINDOW_WIDTH / 4,
            200,
            GameConstants.WINDOW_WIDTH / 2,
            GameConstants.WINDOW_HEIGHT / 2
        );
        storyContentPanel.setOpaque(false);
        storyContentPanel.setVisible(false);
        add(storyContentPanel);
        
        // Phase indicators
        phaseIndicatorsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        phaseIndicatorsPanel.setBounds(0, GameConstants.WINDOW_HEIGHT - 150, GameConstants.WINDOW_WIDTH, 50);
        phaseIndicatorsPanel.setOpaque(false);
        
        phaseLabels = new JLabel[3];
        for (int i = 0; i < 3; i++) {
            phaseLabels[i] = new JLabel(phaseNames[i]);
            phaseLabels[i].setFont(new Font("SansSerif", Font.BOLD, 18));
            phaseLabels[i].setForeground(Color.GRAY);
            phaseLabels[i].setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            phaseIndicatorsPanel.add(phaseLabels[i]);
        }
        
        phaseIndicatorsPanel.setVisible(false);
        add(phaseIndicatorsPanel);
    }
    
    /**
     * Set up animation timers for transitions
     */
    private void setupAnimationTimers() {
        // Fade in timer
        fadeInTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (alphaLevel < 1.0f) {
                    alphaLevel += 0.05f;
                    if (alphaLevel > 1.0f) {
                        alphaLevel = 1.0f;
                    }
                    repaint();
                } else {
                    isFadingIn = false;
                    fadeInTimer.stop();
                }
            }
        });
        
        // Fade out timer
        fadeOutTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (alphaLevel > 0.0f) {
                    alphaLevel -= 0.05f;
                    if (alphaLevel < 0.0f) {
                        alphaLevel = 0.0f;
                    }
                    repaint();
                } else {
                    isFadingOut = false;
                    fadeOutTimer.stop();
                    
                    // Trigger the next phase after fade out completes
                    transitionToNextPhase();
                }
            }
        });
    }
    
    /**
     * Start the story presentation
     */
    public void startStory() {
        // Reset state
        alphaLevel = 0.0f;
        currentPhase = -1;
        
        // Make all components invisible initially
        titleLabel.setVisible(false);
        storyContentPanel.setVisible(false);
        phaseIndicatorsPanel.setVisible(false);
        
        // Start fade in animation
        isFadingIn = true;
        fadeInTimer.start();
        
        // Start the narrative system
        narrativeSystem.startNarrative();
        
        // Show prologue dialogues after a short delay
        Timer delayTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show title
                titleLabel.setVisible(true);
                
                // Start prologue dialogue
                List<NarrativeSystem.DialogueEntry> prologueDialogues = 
                    narrativeSystem.getNextDialogueSequence();
                dialogueManager.startDialogue(prologueDialogues);
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }
    
    /**
     * Handle dialogue sequence end event
     */
    public void onDialogueSequenceEnded() {
        // Determine what to do based on current phase
        if (currentPhase == -1) {
            // Prologue ended, transition to Phase 1
            startPhaseTransition(0);
        } else if (currentPhase >= 0 && currentPhase < 3) {
            // Phase dialogue ended, signal controller to start gameplay
            controller.startPhaseGameplay(currentPhase + 1);
        }
    }
    
    /**
     * Start transition to a specific phase
     */
    private void startPhaseTransition(int phaseIndex) {
        // Start fade out animation
        isFadingOut = true;
        fadeOutTimer.start();
        
        // Store the target phase index for the transition
        currentPhase = phaseIndex;
    }
    
    /**
     * Start dynamic dialogue for a specific phase
     * @param phase The phase number (1-3)
     */
    public void startDynamicPhaseDialogue(int phase) {
        currentPhase = phase;

        // Get the potion types from TimSortVisualization
        // This requires a reference to the TimSortVisualization instance
        String leftPotionType = "Fire Resistance"; // Default
        String rightPotionType = "Strength";       // Default

        // If we have a reference to the TimSortVisualization, get the actual values
        // timSortViz.getLeftGroupPotionType() and timSortViz.getRightGroupPotionType()

        // Update phase indicators
        for (int i = 0; i < phaseLabels.length; i++) {
            if (i == currentPhase) {
                phaseLabels[i].setForeground(Color.WHITE);
                phaseLabels[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            } else if (i < currentPhase) {
                // Completed phases
                phaseLabels[i].setForeground(new Color(200, 200, 200));
                phaseLabels[i].setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            } else {
                // Future phases
                phaseLabels[i].setForeground(Color.GRAY);
                phaseLabels[i].setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            }
        }

        // Show phase indicators
        phaseIndicatorsPanel.setVisible(true);

        // Get dynamic dialogue for this phase
        List<NarrativeSystem.DialogueEntry> dialogueSequence = 
            narrativeSystem.getDynamicDialogue(phase, leftPotionType, rightPotionType);

        // Start the dialogue
        dialogueManager.startDialogue(dialogueSequence);
    }

    /**
     * Show boss battle result with dynamic dialogue
     */
    public void showBossBattleResult(boolean success, int bossLevel) {
        // Get the potion types and selected potion from TimSortVisualization
        String leftPotionType = "Fire Resistance"; // Default
        String rightPotionType = "Strength";       // Default
        String selectedPotion = "Unknown Potion";  // Default

        // If we have a reference to the TimSortVisualization, get the actual values

        // Set the boss battle outcome in narrative system with dynamic details
        narrativeSystem.setBossBattleOutcome(success, bossLevel, selectedPotion);

        // Get appropriate dialogue sequence - now with dynamic content
        String dialogueKey = success ? "boss" + bossLevel + "_success" : "boss" + bossLevel + "_failure";
        List<NarrativeSystem.DialogueEntry> battleDialogues = 
            narrativeSystem.getDialogueSequence(dialogueKey);

        // Set a special flag to indicate this is a boss battle result dialogue
        dialogueManager.setBossBattleResultDialogue(true);

        // Start the dialogue
        dialogueManager.startDialogue(battleDialogues);
    }

    
    
    
    
    
    
    /**
     * Complete transition to next phase after fade out
     */
    private void transitionToNextPhase() {
        // Update UI for the new phase
        titleLabel.setText(phaseNames[currentPhase]);
        
        // Update phase indicators
        for (int i = 0; i < phaseLabels.length; i++) {
            if (i == currentPhase) {
                phaseLabels[i].setForeground(Color.WHITE);
                phaseLabels[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            } else if (i < currentPhase) {
                // Completed phases
                phaseLabels[i].setForeground(new Color(200, 200, 200));
                phaseLabels[i].setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            } else {
                // Future phases
                phaseLabels[i].setForeground(Color.GRAY);
                phaseLabels[i].setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            }
        }
        
        // Show phase indicators
        phaseIndicatorsPanel.setVisible(true);
        
        // Start fade in animation
        isFadingIn = true;
        fadeInTimer.start();
        
        // After a delay, show the dialogue for this phase
        Timer dialogueDelayTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get and display the dialogue for the current phase
                List<NarrativeSystem.DialogueEntry> phaseDialogues = 
                    narrativeSystem.getNextDialogueSequence();
                dialogueManager.startDialogue(phaseDialogues);
            }
        });
        dialogueDelayTimer.setRepeats(false);
        dialogueDelayTimer.start();
    }
    
    /**
     * Start the specific phase dialogue (used when returning from gameplay)
     */
    public void startPhaseDialogue(int phaseIndex, String dialogueKey) {
        currentPhase = phaseIndex;
        
        // Update phase indicators
        for (int i = 0; i < phaseLabels.length; i++) {
            if (i == currentPhase) {
                phaseLabels[i].setForeground(Color.WHITE);
                phaseLabels[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            } else if (i < currentPhase) {
                // Completed phases
                phaseLabels[i].setForeground(new Color(200, 200, 200));
                phaseLabels[i].setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            } else {
                // Future phases
                phaseLabels[i].setForeground(Color.GRAY);
                phaseLabels[i].setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            }
        }
        
        // Show phase indicators
        phaseIndicatorsPanel.setVisible(true);
        
        // Get and display the dialogue sequence
        List<NarrativeSystem.DialogueEntry> dialogueSequence = 
            narrativeSystem.getDialogueSequence(dialogueKey);
        dialogueManager.startDialogue(dialogueSequence);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Set composite for fade effects
        Composite originalComposite = g2d.getComposite();
        if (isFadingIn || isFadingOut) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaLevel));
        }
        
        // Draw background image
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        }
        
        // Restore original composite
        g2d.setComposite(originalComposite);
    }
    
    
    
    
    /**
    * Start the Level 2 story presentation
    */
    public void startLevel2Story() {
        // Reset state
        alphaLevel = 0.0f;
        currentPhase = -1;

        // Make all components invisible initially
        titleLabel.setVisible(false);
        storyContentPanel.setVisible(false);
        phaseIndicatorsPanel.setVisible(false);

        // Start fade in animation
        isFadingIn = true;
        fadeInTimer.start();

        // Set the title text for Level 2
        titleLabel.setText("The Corruption of Toxitar");

        // Show Level 2 intro dialogues after a short delay
        Timer delayTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show title
                titleLabel.setVisible(true);

                // Start Level 2 intro dialogue
                List<NarrativeSystem.DialogueEntry> level2IntroDialogues = 
                    narrativeSystem.getDialogueSequence("level2_intro");
                dialogueManager.startDialogue(level2IntroDialogues);
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }
    
    
    
    
    /**
    * Show transition dialogue between levels
    */
    public void showTransitionDialogue(List<NarrativeSystem.DialogueEntry> dialogueSequence, Runnable onCompleteAction) {
        // Create semi-transparent overlay
        JPanel dialogueOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Dark semi-transparent background (70% opacity)
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        dialogueOverlay.setLayout(null);
        dialogueOverlay.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        dialogueOverlay.setOpaque(false);

        // Add overlay
        add(dialogueOverlay, 0);

        // Create a dialogue manager specifically for this overlay
        DialogueManager transitionDialogueManager = new DialogueManager(controller);
        transitionDialogueManager.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        dialogueOverlay.add(transitionDialogueManager);

        // Set a listener to run the callback when dialogue ends
        transitionDialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
            @Override
            public void onDialogueEnd() {
                // Remove the overlay
                remove(dialogueOverlay);
                repaint();

                // Execute the callback action
                if (onCompleteAction != null) {
                    onCompleteAction.run();
                }
            }
        });

        // Start the dialogue sequence
        transitionDialogueManager.startDialogue(dialogueSequence);
    }
    
    
    
    /**
    * Show boss battle result for Level 2 (Toxitar)
    */
    public void showLevel2BossBattleResult(boolean success, String selectedPotion) {
        // Get appropriate dialogue key based on outcome
        String dialogueKey = success ? "boss2_success" : "boss2_failure";

        // Get the dialogue sequence from NarrativeSystem
        List<NarrativeSystem.DialogueEntry> battleDialogues = narrativeSystem.getDialogueSequence(dialogueKey);

        // If not found, use the dynamic method
        if (battleDialogues == null || battleDialogues.isEmpty()) {
            battleDialogues = narrativeSystem.getToxitarBattleOutcomeDialogue(success, selectedPotion);
        }

        // Set a special flag to indicate this is a boss battle result dialogue
        dialogueManager.setBossBattleResultDialogue(true);

        // Start the dialogue
        dialogueManager.startDialogue(battleDialogues);
    }
    
    /**
    * Start dynamic dialogue for a specific phase in Level 2
    */
    public void startLevel2PhaseDialogue(int phase) {
        currentPhase = phase;

        // Get the potion types
        String leftPotionType = "Dexterity"; // Default for Level 2
        String rightPotionType = "Strength"; // Default for Level 2

        // Update phase indicators
        for (int i = 0; i < phaseLabels.length; i++) {
            if (i == currentPhase) {
                phaseLabels[i].setForeground(Color.WHITE);
                phaseLabels[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            } else if (i < currentPhase) {
                // Completed phases
                phaseLabels[i].setForeground(new Color(200, 200, 200));
                phaseLabels[i].setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            } else {
                // Future phases
                phaseLabels[i].setForeground(Color.GRAY);
                phaseLabels[i].setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            }
        }

        // Show phase indicators
        phaseIndicatorsPanel.setVisible(true);

        // Get dynamic dialogue for Level 2 phase
        List<NarrativeSystem.DialogueEntry> dialogueSequence = 
            narrativeSystem.getDynamicLevel2Dialogue(phase, leftPotionType, rightPotionType);

        // Start the dialogue
        dialogueManager.startDialogue(dialogueSequence);
    }
}