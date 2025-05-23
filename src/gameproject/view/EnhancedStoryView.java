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
import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
    
    // Background management
    private Map<String, ImageIcon> backgroundCache = new HashMap<>();
    private ImageIcon currentBackground;
    private int currentLevel = 1; // Default to Level 1
    
    // Background paths for different phases and levels
    private static final Map<Integer, Map<String, String>> BACKGROUND_PATHS = new HashMap<>();
    static {
        // Level 1 backgrounds
        Map<String, String> level1Backgrounds = new HashMap<>();
        level1Backgrounds.put("prologue", "/gameproject/resources/backgrounds/village_bg.png");
        level1Backgrounds.put("phase1", "/gameproject/resources/backgrounds/forest_bg.png");
        level1Backgrounds.put("phase2", "/gameproject/resources/backgrounds/scholars_library_bg.png");
        level1Backgrounds.put("phase3", "/gameproject/resources/backgrounds/alchemy_laboratory_bg.png");
        level1Backgrounds.put("boss", "/gameproject/resources/backgrounds/village_destroyed_bg.png");
        BACKGROUND_PATHS.put(1, level1Backgrounds);

        // Level 2 backgrounds (placeholders for now)
        Map<String, String> level2Backgrounds = new HashMap<>();
        level2Backgrounds.put("prologue", "/gameproject/resources/backgrounds/market_bg.png");
        level2Backgrounds.put("phase1", "/gameproject/resources/backgrounds/mountain_bg.png");
        level2Backgrounds.put("phase2", "/gameproject/resources/backgrounds/scholars_library_bg.png");
        level2Backgrounds.put("phase3", "/gameproject/resources/backgrounds/lab_bg.png");
        level2Backgrounds.put("boss", "/gameproject/resources/backgrounds/toxitar_battle_scene.png");
        BACKGROUND_PATHS.put(2, level2Backgrounds);

        // Level 3 backgrounds (placeholders for now)
        Map<String, String> level3Backgrounds = new HashMap<>();
        level3Backgrounds.put("prologue", "/gameproject/resources/backgrounds/castle_bg.png");
        level3Backgrounds.put("phase1", "/gameproject/resources/backgrounds/desert_bg.png");
        level3Backgrounds.put("phase2", "/gameproject/resources/backgrounds/scholars_library_bg.png");
        level3Backgrounds.put("phase3", "/gameproject/resources/backgrounds/alchemy_bg.png");
        level3Backgrounds.put("boss", "/gameproject/resources/backgrounds/lord_chaosa_battle_scene.png");
        BACKGROUND_PATHS.put(3, level3Backgrounds);
    }
    
    
    
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
    
    private boolean skipToGameplay = false;
    private boolean battleDialogueCompleted = false;
    
    
    /**
     * Constructor - Initialize the enhanced story view
     */
    public EnhancedStoryView(GameController controller) {
        this.controller = controller;
        this.narrativeSystem = NarrativeSystem.getInstance();
        this.resourceManager = ResourceManager.getInstance();
        
        // Use null layout for precise component positioning
        setLayout(null);
        
        // Initialize UI components
        initializeUI();
        
        // Initialize dialogue manager
        dialogueManager = new DialogueManager(controller);
        dialogueManager.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        add(dialogueManager);
        
        // Set up animation timers
        setupAnimationTimers();
        
        // Load initial background
        loadBackground("prologue");
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
     * Load the background for a specific game phase
     */
    private void loadBackground(String phase) {
        // Get the current level from the controller
        currentLevel = controller.model.getGameLevel();
        if (currentLevel < 1 || currentLevel > 3) {
            currentLevel = 1; // Default to Level 1 if invalid
        }

        System.out.println("DEBUG: Loading background for Level " + currentLevel + ", phase " + phase);

        // Get the background path for this level and phase
        Map<String, String> levelBackgrounds = BACKGROUND_PATHS.get(currentLevel);
        if (levelBackgrounds == null) {
            System.err.println("No backgrounds defined for level " + currentLevel);
            return;
        }

        String backgroundPath = levelBackgrounds.get(phase);
        if (backgroundPath == null) {
            System.err.println("No background defined for phase " + phase + " in level " + currentLevel);
            return;
        }

        System.out.println("DEBUG: Attempting to load background: " + backgroundPath);

        // Check if we've already loaded this background
        if (backgroundCache.containsKey(backgroundPath)) {
            currentBackground = backgroundCache.get(backgroundPath);
            System.out.println("DEBUG: Using cached background: " + backgroundPath);
            return;
        }

        // Load the background image and force a fresh load (don't use cache)
        ImageIcon background = null;
        try {
            // First try directly with resource manager
            background = resourceManager.getImage(backgroundPath);

            // If not found, check for file existence and report
            if (background == null) {
                System.err.println("Failed to load background through resource manager: " + backgroundPath);
                String resourcePath = getClass().getResource(backgroundPath).getPath();
                File file = new File(resourcePath);
                if (file.exists()) {
                    System.out.println("DEBUG: File exists but could not be loaded: " + resourcePath);
                } else {
                    System.out.println("DEBUG: File does not exist: " + resourcePath);
                }
            } else {
                System.out.println("DEBUG: Successfully loaded background: " + backgroundPath);
            }
        } catch (Exception e) {
            System.err.println("Error loading background: " + e.getMessage());
        }

        // If background image is not found, create a fallback gradient
        if (background == null) {
            System.err.println("Failed to load background: " + backgroundPath + " - Using fallback gradient");

            // Create a fallback gradient background
            int width = GameConstants.WINDOW_WIDTH;
            int height = GameConstants.WINDOW_HEIGHT;

            BufferedImage fallbackImage = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = fallbackImage.createGraphics();

            // Create gradient from dark blue to lighter blue
            Color startColor, endColor;

            // Use different colors based on level and phase for visual distinction
            if (currentLevel == 2) {
                // Level 2 - greenish for Toxitar
                startColor = new Color(10, 50, 20);
                endColor = new Color(30, 80, 40);
            } else if (currentLevel == 3) {
                // Level 3 - purplish for Lord Chaosa
                startColor = new Color(40, 10, 50);
                endColor = new Color(60, 30, 80);
            } else {
                // Level 1 - bluish default
                startColor = new Color(20, 30, 60);
                endColor = new Color(50, 70, 120);
            }

            GradientPaint gradient = new GradientPaint(
                0, 0, startColor,
                0, height, endColor
            );

            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, width, height);
            g2d.dispose();

            background = new ImageIcon(fallbackImage);
        }

        // Cache and set the background
        backgroundCache.put(backgroundPath, background);
        currentBackground = background;
    }

    
    
    
    
    
    
    /**
     * Start the story presentation
     */
    public void startStory() {
        // Reset the skip flag
        skipToGameplay = false;

        // Reset state
        alphaLevel = 0.0f;
        currentPhase = -1;

        // Make all components invisible initially
        titleLabel.setVisible(false);
        storyContentPanel.setVisible(false);
        phaseIndicatorsPanel.setVisible(false);

        // Load the prologue background
        loadBackground("prologue");

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

                // IMPORTANT: Set dialogue end listener before starting dialogue
                dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                    @Override
                    public void onDialogueEnd() {
                        // This should trigger gameplay for Level 1
                        System.out.println("DEBUG: Level 1 prologue dialogue ended, starting Phase 1");
                        skipToGameplay = true;
                        controller.startPhaseGameplay(1);
                    }
                });

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
        // If we're already set to skip to gameplay, don't do anything
        if (skipToGameplay) {
            System.out.println("DEBUG: Dialogue sequence ended, but skipToGameplay is true - letting controller handle it");
            return;
        }

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
    * Start dynamic dialogue for a specific phase with proper potion type retrieval
    * @param phase The phase number (1-3)
    */
    public void startDynamicPhaseDialogue(int phase) {
        currentPhase = phase;

        // Load the appropriate background for this phase
        String phaseKey = "phase" + phase;
        loadBackground(phaseKey);

        // Get the potion types from the model
        String leftPotionType = controller.model.getLeftPotionType();
        String rightPotionType = controller.model.getRightPotionType();

        System.out.println("DEBUG: Retrieved potion types for dialogue: " + leftPotionType + ", " + rightPotionType);

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

        // Determine if we're in Level 2 or 3 to get the appropriate dialogue
        int gameLevel = controller.model.getGameLevel();
        List<NarrativeSystem.DialogueEntry> dialogueSequence;

        if (gameLevel == 3) {
            // Get Level 3 specific dialogue
            dialogueSequence = narrativeSystem.getDynamicLevel3Dialogue(phase, leftPotionType, rightPotionType);
        } else if (gameLevel == 2) {
            // Get Level 2 specific dialogue
            dialogueSequence = narrativeSystem.getDynamicLevel2Dialogue(phase, leftPotionType, rightPotionType);
        } else {
            // Get standard dialogue for Level 1
            dialogueSequence = narrativeSystem.getDynamicDialogue(phase, leftPotionType, rightPotionType);
        }

        // Start the dialogue
        dialogueManager.startDialogue(dialogueSequence);
    }

    /**
    * Show boss battle result with dynamic dialogue and proper fade transitions
    */
    public void showBossBattleResult(boolean success, int bossLevel) {
        // Load the boss battle background - IMPORTANT CHANGE
        loadBackground("boss");
        System.out.println("DEBUG: Loaded boss battle background for level " + currentLevel);

        // Get the selected potion
        String selectedPotion = controller.model.getSelectedPotion();
        System.out.println("DEBUG: Selected potion: " + selectedPotion);

        setVisible(true);

        // Get appropriate dialogue sequence
        String dialogueKey = success ? "boss" + bossLevel + "_success" : "boss" + bossLevel + "_failure";
        List<NarrativeSystem.DialogueEntry> battleDialogues = 
            narrativeSystem.getDialogueSequence(dialogueKey);

        // IMPORTANT: Make sure dialogueManager is visible and on top
        dialogueManager.setVisible(true);
        if (getComponentZOrder(dialogueManager) != 0) {
            setComponentZOrder(dialogueManager, 0);
        }

        // CRITICAL FIX: Set the boss battle result flag before starting dialogue
        dialogueManager.setBossBattleResultDialogue(true);

        // Handle successful battle with a black screen transition
        if (success) {
            dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                @Override
                public void onDialogueEnd() {
                    System.out.println("DEBUG: Battle success dialogue ended, now showing fade transition");

                    // Create a black screen overlay with initial transparency of 0
                    final JPanel blackScreen = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            g.setColor(Color.BLACK);
                            g.fillRect(0, 0, getWidth(), getHeight());
                        }
                    };
                    blackScreen.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
                    blackScreen.setOpaque(true);

                    // Make it initially transparent
                    blackScreen.setBackground(new Color(0, 0, 0, 0));
                    add(blackScreen, 0);

                    // Create fade OUT animation (battle dialogue to black)
                    final float[] alpha = {0.0f};
                    final int FADE_DURATION = 1000; // 1 second fade
                    final int FRAMES = 20; // 20 animation frames

                    Timer fadeOutTimer = new Timer(FADE_DURATION/FRAMES, new ActionListener() {
                        int frame = 0;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame++;
                            // Calculate alpha based on current frame
                            alpha[0] = (float)frame / FRAMES;

                            // Update black screen opacity
                            blackScreen.setBackground(new Color(0, 0, 0, Math.min(1.0f, alpha[0])));

                            // When fade out is complete
                            if (frame >= FRAMES) {
                                ((Timer)e.getSource()).stop();

                                // Hide the dialogue manager now that screen is black
                                dialogueManager.setVisible(false);

                                // Wait for 1 second with black screen
                                Timer blackScreenTimer = new Timer(1000, ev -> {
                                    // CRITICAL FIX: Record progress for Level 1 completion here
                                    // This is the important line to add:
                                    controller.progressTracker.completeLevel("Beginner", 1, 3);
                                    System.out.println("DEBUG: Level 1 completion recorded! Stars: 3");

                                    // Create a fresh dialogue manager for transition dialogue
                                    remove(dialogueManager);  // Remove old dialogue manager
                                    dialogueManager = new DialogueManager(controller);
                                    dialogueManager.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
                                    add(dialogueManager);

                                    // Ensure the black screen is on top
                                    setComponentZOrder(blackScreen, 0);

                                    // Start fade IN (black to transition dialogue)
                                    final float[] fadeInAlpha = {1.0f};

                                    Timer fadeInTimer = new Timer(FADE_DURATION/FRAMES, new ActionListener() {
                                        int fadeInFrame = 0;

                                        @Override
                                        public void actionPerformed(ActionEvent evt) {
                                            fadeInFrame++;

                                            // Calculate alpha based on current frame (decreasing)
                                            fadeInAlpha[0] = 1.0f - ((float)fadeInFrame / FRAMES);

                                            // Update black screen opacity (getting more transparent)
                                            blackScreen.setBackground(new Color(0, 0, 0, Math.max(0.0f, fadeInAlpha[0])));

                                            // When fade in is complete
                                            if (fadeInFrame >= FRAMES) {
                                                ((Timer)evt.getSource()).stop();

                                                // Remove black screen when completely faded in
                                                remove(blackScreen);

                                                // Load transition dialogue background
                                                loadBackground("prologue");  // Use village background for transition dialogue too

                                                // CRUCIAL FIX: Don't call onBossBattleComplete again!
                                                // Instead, directly show the level transition dialogue
                                                List<NarrativeSystem.DialogueEntry> transitionDialogues = 
                                                    narrativeSystem.getDialogueSequence("level1to2_transition");

                                                // CRITICAL FIX: Set a new listener for the transition dialogue completion
                                                dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                                                    @Override
                                                    public void onDialogueEnd() {
                                                        // Make sure progress is saved before returning to level selection
                                                        controller.progressTracker.saveProgress();

                                                        // Finally navigate to level selection
                                                        controller.showLevelSelection();
                                                    }
                                                });

                                                // Start the transition dialogue
                                                dialogueManager.startDialogue(transitionDialogues);

                                                // Force refresh
                                                revalidate();
                                                repaint();
                                            }
                                        }
                                    });

                                    // Start the fade in animation
                                    fadeInTimer.start();
                                });
                                blackScreenTimer.setRepeats(false);
                                blackScreenTimer.start();
                            }
                        }
                    });

                    // Start the fade out animation
                    fadeOutTimer.start();

                    // Force refresh to start fade
                    revalidate();
                    repaint();
                }
            });
        }

        // Set game state and start dialogue
        controller.model.setCurrentState(GameState.STORY_MODE);
        dialogueManager.startDialogue(battleDialogues);

        // Force repaint
        revalidate();
        repaint();
    }

    //endddd
    
    
    
    
    
    /**
     * Complete transition to next phase after fade out
     */
    private void transitionToNextPhase() {
        // Load the background for the new phase
        String phaseKey = "phase" + currentPhase;
        loadBackground(phaseKey);
        
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

        // Draw current background image
        if (currentBackground != null) {
            g2d.drawImage(currentBackground.getImage(), 0, 0, getWidth(), getHeight(), this);
        }

        // Restore original composite
        g2d.setComposite(originalComposite);
    }

    
    
    
    /**
    * Start the Level 2 story presentation
    */
    public void startLevel2Story() {
        // Reset skip flag
        skipToGameplay = false;

        // Reset state
        alphaLevel = 0.0f;
        currentPhase = -1;

        // Clear the background cache to force reload
        backgroundCache.clear();

        // Make all components invisible initially
        titleLabel.setVisible(false);
        storyContentPanel.setVisible(false);
        phaseIndicatorsPanel.setVisible(false);

        // Explicitly set current level
        currentLevel = 2;

        // Load the prologue background for Level 2
        loadBackground("prologue");

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

                // Get the dialogue sequence
                List<NarrativeSystem.DialogueEntry> level2IntroDialogues = 
                    narrativeSystem.getDialogueSequence("level2_intro");

                // IMPORTANT: Set a completion handler BEFORE starting dialogue
                dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                    @Override
                    public void onDialogueEnd() {
                        System.out.println("DEBUG: Level 2 intro dialogue ended, starting Phase 1");
                        skipToGameplay = true;
                        // Explicitly start Phase 1 of Level 2
                        controller.startPhaseGameplay(1);
                    }
                });

                // Start the dialogue
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
    * Show boss battle result for Level 2 (Toxitar) with fade transition but NO TIMER
    */
    public void showLevel2BossBattleResult(boolean success, String selectedPotion) {
        // Get the potion from the model if not provided
        if (selectedPotion == null || selectedPotion.isEmpty()) {
            selectedPotion = controller.model.getSelectedPotion();
            System.out.println("DEBUG: Retrieved selected potion from model: " + selectedPotion);
        }

        // IMPORTANT: Load the Toxitar boss battle background
        currentLevel = 2; // Ensure correct level is set
        loadBackground("boss");
        System.out.println("DEBUG: Loaded Toxitar boss battle background");

        setVisible(true);

        // Get dynamic dialogue from NarrativeSystem
        List<NarrativeSystem.DialogueEntry> battleDialogues = 
            narrativeSystem.getToxitarBattleOutcomeDialogue(success, selectedPotion);

        // CRITICAL FIX: Create a fresh dialogue manager to ensure proper rendering
        remove(dialogueManager);
        dialogueManager = new DialogueManager(controller);
        dialogueManager.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        add(dialogueManager);

        // CRITICAL FIX: Ensure this panel is on top and visible
        setComponentZOrder(dialogueManager, 0);
        dialogueManager.setVisible(true);
        System.out.println("DEBUG: Created fresh dialogue manager and set to visible");

        // Set the boss battle result flag
        dialogueManager.setBossBattleResultDialogue(true);

        // Handle successful battle with a black screen transition
        if (success) {
            dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                @Override
                public void onDialogueEnd() {
                    System.out.println("DEBUG: Battle success dialogue ended, now showing fade transition");

                    // Create a black screen overlay with initial transparency of 0
                    final JPanel blackScreen = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            g.setColor(Color.BLACK);
                            g.fillRect(0, 0, getWidth(), getHeight());
                        }
                    };
                    blackScreen.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
                    blackScreen.setOpaque(true);

                    // Make it initially transparent
                    blackScreen.setBackground(new Color(0, 0, 0, 0));
                    add(blackScreen, 0);

                    // Create fade OUT animation (battle dialogue to black)
                    final float[] alpha = {0.0f};
                    final int FADE_DURATION = 1000; // 1 second fade
                    final int FRAMES = 20; // 20 animation frames

                    Timer fadeOutTimer = new Timer(FADE_DURATION/FRAMES, new ActionListener() {
                        int frame = 0;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame++;
                            // Calculate alpha based on current frame
                            alpha[0] = (float)frame / FRAMES;

                            // Update black screen opacity
                            blackScreen.setBackground(new Color(0, 0, 0, Math.min(1.0f, alpha[0])));

                            // When fade out is complete
                            if (frame >= FRAMES) {
                                ((Timer)e.getSource()).stop();

                                // Hide the dialogue manager now that screen is black
                                dialogueManager.setVisible(false);

                                // Wait for 1 second with black screen
                                Timer blackScreenTimer = new Timer(1000, ev -> {
                                    // Record progress if successful
                                    if (success) {
                                        controller.progressTracker.completeLevel("Intermediate", 1, 3);
                                    }

                                    // CRITICAL FIX: Create a fresh dialogue manager for level 2 to 3 transition
                                    remove(dialogueManager);
                                    dialogueManager = new DialogueManager(controller);
                                    dialogueManager.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
                                    add(dialogueManager);

                                    // Start fade IN (black to transition dialogue)
                                    final float[] fadeInAlpha = {1.0f};

                                    Timer fadeInTimer = new Timer(FADE_DURATION/FRAMES, new ActionListener() {
                                        int fadeInFrame = 0;

                                        @Override
                                        public void actionPerformed(ActionEvent evt) {
                                            fadeInFrame++;

                                            // Calculate alpha based on current frame (decreasing)
                                            fadeInAlpha[0] = 1.0f - ((float)fadeInFrame / FRAMES);

                                            // Update black screen opacity (getting more transparent)
                                            blackScreen.setBackground(new Color(0, 0, 0, Math.max(0.0f, fadeInAlpha[0])));

                                            // When fade in is complete
                                            if (fadeInFrame >= FRAMES) {
                                                ((Timer)evt.getSource()).stop();

                                                // Remove black screen when completely faded in
                                                remove(blackScreen);

                                                // Load the proper background for transition dialogue
                                                currentLevel = 2; // Ensure correct level is set
                                                loadBackground("prologue");

                                                // Get Level 2 to Level 3 transition dialogue
                                                List<NarrativeSystem.DialogueEntry> transitionDialogues = 
                                                    narrativeSystem.getDialogueSequence("level2to3_transition");

                                                // CRITICAL FIX: Set a new listener for the transition dialogue completion
                                                dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                                                    @Override
                                                    public void onDialogueEnd() {
                                                        // Finally navigate to level selection
                                                        controller.showLevelSelection();
                                                    }
                                                });

                                                // Start the transition dialogue
                                                dialogueManager.startDialogue(transitionDialogues);

                                                // Force refresh
                                                revalidate();
                                                repaint();
                                            }
                                        }
                                    });

                                    // Start the fade in animation
                                    fadeInTimer.start();
                                });
                                blackScreenTimer.setRepeats(false);
                                blackScreenTimer.start();
                            }
                        }
                    });

                    // Start the fade out animation
                    fadeOutTimer.start();

                    // Force refresh to start fade
                    revalidate();
                    repaint();
                }
            });
        } else {
            // For failed battles, handle as before
            dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                @Override
                public void onDialogueEnd() {
                    System.out.println("DEBUG: Battle failure dialogue ended, showing level selection");
                    controller.showLevelSelection();
                }
            });
        }

        // Set game state and start dialogue
        controller.model.setCurrentState(GameState.STORY_MODE);

        // CRITICAL FIX: Add debugging to trace dialogue starting
        System.out.println("DEBUG: Starting battle dialogue with " + battleDialogues.size() + " entries");
        dialogueManager.startDialogue(battleDialogues);

        // CRITICAL FIX: Ensure UI is updated
        revalidate();
        repaint();
    }

    
    /**
    * Start dynamic dialogue for a specific phase in Level 2
    */
    public void startLevel2PhaseDialogue(int phase) {
        currentPhase = phase;

        
        
        // If there are any existing dialogue panels, remove them
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel && comp != dialogueManager) {
                remove(comp);
            }
        }

        // Make sure we're using a single dialogueManager instance
        if (dialogueManager != null) {
            // Clear any previous dialogue
            dialogueManager.setVisible(false);
        }
        
        
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

        // Get dynamic dialogue for Level 2 phase
        List<NarrativeSystem.DialogueEntry> dialogueSequence = 
            narrativeSystem.getDynamicLevel2Dialogue(phase, leftPotionType, rightPotionType);

        // Start the dialogue with clean slate
        dialogueManager.setVisible(true);
        dialogueManager.startDialogue(dialogueSequence);

        // Force repaint to ensure clean display
        revalidate();
        repaint();
    }
    
    
    
    
    
    
    /**
    * Start the Level 3 story presentation
    */
    public void startLevel3Story() {
        // Reset skip flag
        skipToGameplay = false;

        // Reset state
        alphaLevel = 0.0f;
        currentPhase = -1;

        // Clear the background cache to force reload
        backgroundCache.clear();

        // Make all components invisible initially
        titleLabel.setVisible(false);
        storyContentPanel.setVisible(false);
        phaseIndicatorsPanel.setVisible(false);

        // Explicitly set current level
        currentLevel = 3;

        // Load the prologue background
        loadBackground("prologue");

        // Start fade in animation
        isFadingIn = true;
        fadeInTimer.start();

        // Set the title text for Level 3
        titleLabel.setText("The Reality Warper: Lord Chaosa");

        // Show Level 3 intro dialogues after a short delay
        Timer delayTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show title
                titleLabel.setVisible(true);

                // Get the dialogue sequence
                List<NarrativeSystem.DialogueEntry> level3IntroDialogues = 
                    narrativeSystem.getDialogueSequence("level3_intro");

                // IMPORTANT: Set a completion handler BEFORE starting dialogue
                dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                    @Override
                    public void onDialogueEnd() {
                        System.out.println("DEBUG: Level 3 intro dialogue ended, starting Phase 1");
                        skipToGameplay = true;
                        // Explicitly start Phase 1 of Level 3
                        controller.startPhaseGameplay(1);
                    }
                });

                // Start the dialogue
                dialogueManager.startDialogue(level3IntroDialogues);
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
    }
    
    
    
    
    
    /**
    * Start dynamic dialogue for a specific phase in Level 3
    */
    public void startLevel3PhaseDialogue(int phase) {
        currentPhase = phase;

        // If there are any existing dialogue panels, remove them
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel && comp != dialogueManager) {
                remove(comp);
            }
        }

        // Make sure we're using a single dialogueManager instance
        if (dialogueManager != null) {
            // Clear any previous dialogue
            dialogueManager.setVisible(false);
        }

        // Get the potion types
        String leftPotionType = "Strength"; // Default for Level 3
        String rightPotionType = "Cold Resistance"; // Default for Level 3

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

        // Get dynamic dialogue for Level 3 phase
        List<NarrativeSystem.DialogueEntry> dialogueSequence = 
            narrativeSystem.getDynamicLevel3Dialogue(phase, leftPotionType, rightPotionType);

        // Start the dialogue with clean slate
        dialogueManager.setVisible(true);
        dialogueManager.startDialogue(dialogueSequence);

        // Force repaint to ensure clean display
        revalidate();
        repaint();
    }
    
    
    /**
    * Show boss battle result for Level 3 (Lord Chaosa)
    */
    public void showLevel3BossBattleResult(boolean success, String selectedPotion) {
        // Get the potion from the model if not provided
        if (selectedPotion == null || selectedPotion.isEmpty()) {
            selectedPotion = controller.model.getSelectedPotion();
            System.out.println("DEBUG: Retrieved selected potion from model: " + selectedPotion);
        }

        // IMPORTANT: Load the Lord Chaosa boss battle background
        currentLevel = 3; // Ensure correct level is set
        loadBackground("boss");
        System.out.println("DEBUG: Loaded Lord Chaosa boss battle background");

        setVisible(true);

        // Get dynamic dialogue from NarrativeSystem
        List<NarrativeSystem.DialogueEntry> battleDialogues = 
            narrativeSystem.getLordChaosaBattleOutcomeDialogue(success, selectedPotion);

        // CRITICAL FIX: Create a fresh dialogue manager to ensure proper rendering
        remove(dialogueManager);
        dialogueManager = new DialogueManager(controller);
        dialogueManager.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        add(dialogueManager);

        // CRITICAL FIX: Ensure this panel is on top and visible
        setComponentZOrder(dialogueManager, 0);
        dialogueManager.setVisible(true);
        System.out.println("DEBUG: Created fresh dialogue manager and set to visible");

        // Set the boss battle result flag
        dialogueManager.setBossBattleResultDialogue(true);

        // Handle successful battle with a black screen transition
        if (success) {
            dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                @Override
                public void onDialogueEnd() {
                    // Rest of the code remains the same...
                    // [Existing transition code continues here]
                    System.out.println("DEBUG: Battle success dialogue ended, now showing game completion with fade transition");

                    // Create a black screen overlay with initial transparency of 0
                    final JPanel blackScreen = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            g.setColor(Color.BLACK);
                            g.fillRect(0, 0, getWidth(), getHeight());
                        }
                    };
                    blackScreen.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
                    blackScreen.setOpaque(true);

                    // Make it initially transparent
                    blackScreen.setBackground(new Color(0, 0, 0, 0));
                    add(blackScreen, 0);

                    // Create fade OUT animation (battle dialogue to black)
                    final float[] alpha = {0.0f};
                    final int FADE_DURATION = 1000; // 1 second fade
                    final int FRAMES = 20; // 20 animation frames

                    Timer fadeOutTimer = new Timer(FADE_DURATION/FRAMES, new ActionListener() {
                        int frame = 0;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame++;
                            // Calculate alpha based on current frame
                            alpha[0] = (float)frame / FRAMES;

                            // Update black screen opacity
                            blackScreen.setBackground(new Color(0, 0, 0, Math.min(1.0f, alpha[0])));

                            // When fade out is complete
                            if (frame >= FRAMES) {
                                ((Timer)e.getSource()).stop();

                                // Hide the dialogue manager now that screen is black
                                dialogueManager.setVisible(false);

                                // Wait for 1 second with black screen
                                Timer blackScreenTimer = new Timer(1000, ev -> {
                                    // Now start fade IN for completion dialogue

                                    // CRITICAL FIX: Create a fresh dialogue manager for completion dialogue 
                                    remove(dialogueManager);  // Remove old dialogue manager
                                    dialogueManager = new DialogueManager(controller);
                                    dialogueManager.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
                                    add(dialogueManager);

                                    // Ensure the black screen is on top
                                    setComponentZOrder(blackScreen, 0);

                                    // Start fade IN (black to completion dialogue)
                                    final float[] fadeInAlpha = {1.0f};

                                    Timer fadeInTimer = new Timer(FADE_DURATION/FRAMES, new ActionListener() {
                                        int fadeInFrame = 0;

                                        @Override
                                        public void actionPerformed(ActionEvent evt) {
                                            fadeInFrame++;

                                            // Calculate alpha based on current frame (decreasing)
                                            fadeInAlpha[0] = 1.0f - ((float)fadeInFrame / FRAMES);

                                            // Update black screen opacity (getting more transparent)
                                            blackScreen.setBackground(new Color(0, 0, 0, Math.max(0.0f, fadeInAlpha[0])));

                                            // When fade in is complete
                                            if (fadeInFrame >= FRAMES) {
                                                ((Timer)evt.getSource()).stop();

                                                // Remove black screen when completely faded in
                                                remove(blackScreen);

                                                // Load the castle background for the completion dialogue
                                                currentLevel = 3; // Ensure correct level is set
                                                loadBackground("prologue");

                                                // Record completion
                                                controller.progressTracker.completeLevel("Advanced", 1, 3);

                                                // CRITICAL FIX: Set a new listener for the end game completion dialogue
                                                dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                                                    @Override
                                                    public void onDialogueEnd() {
                                                        // Finally navigate to level selection
                                                        controller.showLevelSelection();
                                                    }
                                                });

                                                // Show the completion dialogue
                                                showGameCompletionDialogue();

                                                // Force refresh
                                                revalidate();
                                                repaint();
                                            }
                                        }
                                    });

                                    // Start the fade in animation
                                    fadeInTimer.start();
                                });
                                blackScreenTimer.setRepeats(false);
                                blackScreenTimer.start();
                            }
                        }
                    });

                    // Start the fade out animation
                    fadeOutTimer.start();

                    // Force refresh to start fade
                    revalidate();
                    repaint();
                }
            });
        } else {
            // For failed battles, handle as before
            dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                @Override
                public void onDialogueEnd() {
                    System.out.println("DEBUG: Battle failure dialogue ended, showing level selection");
                    controller.showLevelSelection();
                }
            });
        }

        // Set game state and start dialogue
        controller.model.setCurrentState(GameState.STORY_MODE);

        // CRITICAL FIX: Add debugging to trace dialogue starting
        System.out.println("DEBUG: Starting battle dialogue with " + battleDialogues.size() + " entries");
        dialogueManager.startDialogue(battleDialogues);

        // CRITICAL FIX: Ensure UI is updated
        revalidate();
        repaint();
    }

    
    
    /**
    * Show game completion dialogue after defeating Lord Chaosa
    */
    public void showGameCompletionDialogue() {
        System.out.println("DEBUG: Starting game completion dialogue sequence");

        // Clear any existing dialogue manager or overlays first
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel && comp != dialogueManager && 
                (comp.getName() == null || !comp.getName().equals("backgroundPanel"))) {
                remove(comp);
            }
        }

        // CRITICAL FIX: Ensure we're not reusing any old dialogue manager state
        dialogueManager.setBossBattleResultDialogue(false);

        // Create a simple, very brief fade effect (1 second)
        final JPanel blackoutPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        blackoutPanel.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        blackoutPanel.setOpaque(true);
        add(blackoutPanel, 0);
        System.out.println("DEBUG: Added black screen for transition");

        // Get the game completion dialogue
        List<NarrativeSystem.DialogueEntry> completionDialogues = 
            narrativeSystem.getDialogueSequence("game_completion");

        if (completionDialogues == null || completionDialogues.isEmpty()) {
            System.out.println("ERROR: No game completion dialogues found");
            controller.showMainMenu();
            return;
        }

        // Set title for completion
        titleLabel.setText("The Harmony of Order Restored");
        titleLabel.setVisible(true);

        // Quick 1-second fade transition
        Timer fadeTimer = new Timer(20, new ActionListener() {
            float alpha = 1.0f;
            int frameCount = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                frameCount++;
                // Complete fade in 50 frames (1 second)
                if (frameCount >= 50) {
                    // Remove the black panel
                    remove(blackoutPanel);
                    ((Timer)e.getSource()).stop();

                    // Make dialogue manager visible again
                    dialogueManager.setVisible(true);

                    // CRITICAL FIX: Set a listener to go to level selection (not main menu) when completion dialogue ends
                    dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                        @Override
                        public void onDialogueEnd() {
                            System.out.println("DEBUG: Game completion dialogue ended, returning to level selection");
                            controller.showLevelSelection();
                        }
                    });

                    // Start the dialogue
                    System.out.println("DEBUG: Starting completion dialogue");
                    dialogueManager.startDialogue(completionDialogues);

                    // Force repaint
                    revalidate();
                    repaint();
                } else {
                    // Fade out black panel
                    alpha = 1.0f - (frameCount / 50.0f);
                    blackoutPanel.setBackground(new Color(0, 0, 0, (int)(alpha * 255)));
                    repaint();
                }
            }
        });

        // Start the fade timer immediately
        fadeTimer.start();

        // Force revalidate and repaint
        revalidate();
        repaint();
    }




    
    
    
    /**
    * Show completion dialogue with custom callback
    */
    public void showCompletionDialogue(List<NarrativeSystem.DialogueEntry> dialogueSequence, Runnable onCompleteAction) {
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
        DialogueManager completionDialogueManager = new DialogueManager(controller);
        completionDialogueManager.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        dialogueOverlay.add(completionDialogueManager);

        // Set a listener to run the callback when dialogue ends
        completionDialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
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
        completionDialogueManager.startDialogue(dialogueSequence);
    }
    
}