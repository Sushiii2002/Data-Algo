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
        // Reset the skip flag
        skipToGameplay = false;

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
                                    // Now start fade IN for transition dialogue

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

                                                // CRUCIAL FIX: Don't call onBossBattleComplete again!
                                                // Instead, directly show the level transition dialogue
                                                List<NarrativeSystem.DialogueEntry> transitionDialogues = 
                                                    narrativeSystem.getDialogueSequence("level1to2_transition");

                                                // Record completion
                                                controller.progressTracker.completeLevel("Beginner", bossLevel, 3);

                                                // Start the transition dialogue directly
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

        // Start dialogue
        dialogueManager.startDialogue(battleDialogues);

        // Force repaint
        revalidate();
        repaint();
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
        // Reset skip flag
        skipToGameplay = false;

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

        setVisible(true);

        // Get dynamic dialogue from NarrativeSystem
        List<NarrativeSystem.DialogueEntry> battleDialogues = 
            narrativeSystem.getToxitarBattleOutcomeDialogue(success, selectedPotion);

        // IMPORTANT: Make sure dialogueManager is visible and on top
        dialogueManager.setVisible(true);
        if (getComponentZOrder(dialogueManager) != 0) {
            setComponentZOrder(dialogueManager, 0);
        }

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

                                    // Tell the controller the battle is complete
                                    // This triggers the Level 2 -> Level 3 transition WITHOUT using additional timers
                                    controller.onBossBattleComplete(success, 2);

                                    // Remove the black screen - will be handled in the next scene
                                    remove(blackScreen);

                                    // Force refresh
                                    revalidate();
                                    repaint();
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
        dialogueManager.startDialogue(battleDialogues);

        // Force repaint
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

        // Make all components invisible initially
        titleLabel.setVisible(false);
        storyContentPanel.setVisible(false);
        phaseIndicatorsPanel.setVisible(false);

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

        setVisible(true);

        // Get dynamic dialogue from NarrativeSystem
        List<NarrativeSystem.DialogueEntry> battleDialogues = 
            narrativeSystem.getLordChaosaBattleOutcomeDialogue(success, selectedPotion);

        // IMPORTANT: Make sure dialogueManager is visible and on top
        dialogueManager.setVisible(true);
        if (getComponentZOrder(dialogueManager) != 0) {
            setComponentZOrder(dialogueManager, 0);
        }

        // Handle successful battle with a black screen transition
        if (success) {
    dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
        @Override
        public void onDialogueEnd() {
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

                                // Create a fresh dialogue manager for completion dialogue
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

                                            // Force refresh
                                            revalidate();
                                            repaint();
                                        }
                                    }
                                });

                                // Show the completion dialogue (will be visible as black screen fades out)
                                showGameCompletionDialogue();

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
        dialogueManager.startDialogue(battleDialogues);

        // Force repaint
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

        // Reset dialogueManager state
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

                    // Set a listener to go to main menu when completion dialogue ends
                    dialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                        @Override
                        public void onDialogueEnd() {
                            System.out.println("DEBUG: Game completion dialogue ended, returning to main menu");
                            controller.showMainMenu();
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