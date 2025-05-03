package gameproject.ui;

import gameproject.controller.GameController;
import gameproject.model.NarrativeSystem;
import gameproject.util.ResourceManager;
import gameproject.util.GameConstants;
import gameproject.view.DialogueManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class handles the visualization and interaction for the TimSort algorithm phases
 * in the RPG game, with UI controls from the original game.
 */
public class TimSortVisualization extends JPanel {
    // References
    private GameController controller;
    private ResourceManager resourceManager;
    
    // UI Components
    private JPanel gridPanel;
    private JPanel controlPanel;
    private JLabel phaseLabel;
    private JLabel instructionLabel;
    private JButton abilityButton;
    private JButton checkButton;
    private JButton hintButton;
    
    // Timer and UI controls from original game
    private JButton pauseButton;
    private ImageIcon pauseNormalIcon;
    private ImageIcon pauseHoverIcon;
    private ImageIcon hintNormalIcon;
    private ImageIcon hintHoverIcon;
    private Font pixelifySansFont;
    
    // Game state
    private int currentPhase = 1; // 1: Eye of Pattern, 2: Hand of Balance, 3: Mind of Unity
    private boolean phaseCompleted = false;
    
    // Eye of Pattern phase variables
    private List<IngredientItem> allIngredients = new ArrayList<>();
    private List<IngredientItem> selectedIngredients = new ArrayList<>();
    private List<List<IngredientItem>> identifiedRuns = new ArrayList<>();
    
    // Hand of Balance phase variables
    private List<IngredientItem> leftGroup = new ArrayList<>();
    private List<IngredientItem> rightGroup = new ArrayList<>();
    private boolean isLeftGroupSorted = false;
    private boolean isRightGroupSorted = false;
    
    // Mind of Unity phase variables
    private List<IngredientItem> mergedItems = new ArrayList<>();
    private String craftedPotion = null;
    
    // Constants
    private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 4;
    private static final int MAX_SELECTIONS = 10;
    private static final int INGREDIENT_SIZE = 100;
    private static final int GROUP_SIZE = 5; // Size of each group in phase 2
    
    // Store the potion types identified in Phase 2 - to be used in Phase 3
    private String leftGroupPotionType = "Fire Resistance";  // Default value
    private String rightGroupPotionType = "Strength";    
    
    
    private int gameLevel = 1; // Default to Level 1
    private String currentBossName = "Flameclaw"; // Default to Level 1 boss
    private String[] toxitarHints = new String[3]; 
    private String[] lordChaosaHints = new String[3];
    

   // Add a list to track all active timers
    private List<Timer> activeTimers = new ArrayList<>();
    
    
    
    private ImageIcon backgroundImage = null;
    private Map<String, ImageIcon> backgroundCache = new HashMap<>();

    
    
    
    
    // Stores the custom button images
    private ImageIcon eyeActiveIcon;
    private ImageIcon eyeDisabledIcon;
    private ImageIcon handActiveIcon;
    private ImageIcon handDisabledIcon;
    private ImageIcon mindActiveIcon;
    private ImageIcon mindDisabledIcon;

    private JPanel customAbilityButtonPanel;
    private JLabel abilityIconLabel;
    private JLabel abilityNameLabel;
    private boolean abilityButtonEnabled = true;

    // Current active ability icons (changes based on phase)
    private ImageIcon currentActiveIcon;
    private ImageIcon currentDisabledIcon;
    
    /**
     * Constructor - Initialize the TimSort visualization
     */
    public TimSortVisualization(GameController controller) {
        this.controller = controller;
        this.resourceManager = ResourceManager.getInstance();
        
        // Use absolute positioning for precise control
        setLayout(null);
        
        // Load UI resources
        loadResources();
        
        // Initialize UI components
        initializeUI();
        
        // Generate initial ingredients
        generateIngredients();
        
      
    }
    
    /**
     * Load resources for UI elements
     */
    private void loadResources() {
        // Load fonts
        pixelifySansFont = resourceManager.getFont(GameConstants.FONT_PATH, 25f);
        if (pixelifySansFont == null) {
            // Fallback if custom font can't be loaded
            pixelifySansFont = new Font("Arial", Font.BOLD, 25);
        }
        // Load button images
        pauseNormalIcon = resourceManager.getImage("/gameproject/resources/pause_normal.png");
        pauseHoverIcon = resourceManager.getImage("/gameproject/resources/pause_hover.png");
        hintNormalIcon = resourceManager.getImage("/gameproject/resources/hint_normal.png");
        hintHoverIcon = resourceManager.getImage("/gameproject/resources/hint_hover.png");


        // Scale button icons to 70x70 pixels
        if (pauseNormalIcon != null) {
            Image img = pauseNormalIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            pauseNormalIcon = new ImageIcon(img);
        }

        if (pauseHoverIcon != null) {
            Image img = pauseHoverIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            pauseHoverIcon = new ImageIcon(img);
        }

        if (hintNormalIcon != null) {
            Image img = hintNormalIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            hintNormalIcon = new ImageIcon(img);
        }

        if (hintHoverIcon != null) {
            Image img = hintHoverIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            hintHoverIcon = new ImageIcon(img);
        }


        // Load eye of pattern icons
        System.out.println("DEBUG: Attempting to load eye_of_pattern_active.png");
        eyeActiveIcon = resourceManager.getImage("/gameproject/resources/abilities/eye_of_pattern_active.png");
        System.out.println("DEBUG: Attempting to load eye_of_pattern_disabled.png");
        eyeDisabledIcon = resourceManager.getImage("/gameproject/resources/abilities/eye_of_pattern_disabled.png");


        // Create scaled versions - INCREASED SIZE to 150x150 (from 80x80)
        if (eyeActiveIcon != null) {
            System.out.println("DEBUG: Successfully loaded eye_of_pattern_active.png");
            Image img = eyeActiveIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            eyeActiveIcon = new ImageIcon(img);
        } else {
            System.out.println("WARNING: Could not load eye_of_pattern_active.png");
            // Create a fallback icon so we can still see something
            BufferedImage fallbackImg = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = fallbackImg.createGraphics();
            g2d.setColor(Color.GREEN);
            g2d.fillOval(15, 15, 120, 120);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30)); // Increased font size
            g2d.drawString("Eye", 55, 80);
            g2d.dispose();
            eyeActiveIcon = new ImageIcon(fallbackImg);
        }

        if (eyeDisabledIcon != null) {
            System.out.println("DEBUG: Successfully loaded eye_of_pattern_disabled.png");
            Image img = eyeDisabledIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            eyeDisabledIcon = new ImageIcon(img);
        } else {
            System.out.println("WARNING: Could not load eye_of_pattern_disabled.png");
            // Create a fallback icon
            BufferedImage fallbackImg = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = fallbackImg.createGraphics();
            g2d.setColor(Color.GRAY);
            g2d.fillOval(15, 15, 120, 120);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30)); // Increased font size
            g2d.drawString("Eye", 55, 80);
            g2d.dispose();
            eyeDisabledIcon = new ImageIcon(fallbackImg);
        }
        loadPhaseAbilityIcons(1);
    }
    
    
    // Add a new method to load ability icons for a specific phase
    private void loadPhaseAbilityIcons(int phase) {
        // Clear any previously loaded icons to save memory
        currentActiveIcon = null;
        currentDisabledIcon = null;

        System.out.println("DEBUG: Loading ability icons for phase " + phase);

        // Load the appropriate icons based on phase
        if (phase == 1) {
            // Eye of Pattern
            eyeActiveIcon = resourceManager.getImage("/gameproject/resources/abilities/eye_of_pattern_active.png");
            eyeDisabledIcon = resourceManager.getImage("/gameproject/resources/abilities/eye_of_pattern_disabled.png");

            currentActiveIcon = eyeActiveIcon;
            currentDisabledIcon = eyeDisabledIcon;

            System.out.println("DEBUG: Loaded Eye of Pattern icons");
        } 
        else if (phase == 2) {
            // Hand of Balance
            handActiveIcon = resourceManager.getImage("/gameproject/resources/abilities/hand_of_balance_active.png");
            handDisabledIcon = resourceManager.getImage("/gameproject/resources/abilities/hand_of_balance_disabled.png");

            currentActiveIcon = handActiveIcon;
            currentDisabledIcon = handDisabledIcon;

            System.out.println("DEBUG: Loaded Hand of Balance icons");
        } 
        else if (phase == 3) {
            // Mind of Unity
            mindActiveIcon = resourceManager.getImage("/gameproject/resources/abilities/mind_of_unity_active.png");
            mindDisabledIcon = resourceManager.getImage("/gameproject/resources/abilities/mind_of_unity_disabled.png");

            currentActiveIcon = mindActiveIcon;
            currentDisabledIcon = mindDisabledIcon;

            System.out.println("DEBUG: Loaded Mind of Unity icons");
        }

        // Create scaled versions of the icons
        if (currentActiveIcon != null) {
            Image img = currentActiveIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            currentActiveIcon = new ImageIcon(img);
        } else {
            // Create fallback icon if loading fails
            createFallbackIcon(phase, true);
        }

        if (currentDisabledIcon != null) {
            Image img = currentDisabledIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            currentDisabledIcon = new ImageIcon(img);
        } else {
            // Create fallback icon if loading fails
            createFallbackIcon(phase, false);
        }

        // Update the ability icon label if it exists
        if (abilityIconLabel != null) {
            if (abilityButtonEnabled) {
                abilityIconLabel.setIcon(currentActiveIcon);
            } else {
                abilityIconLabel.setIcon(currentDisabledIcon);
            }
        }

        // Update the ability name text
        String abilityName = getAbilityNameForPhase(phase);
        if (abilityNameLabel != null) {
            abilityNameLabel.setText(abilityName);
        }
    }
    
    
    
    // Helper method to get ability name based on phase
    private String getAbilityNameForPhase(int phase) {
        switch (phase) {
            case 1: return "Eye of Pattern";
            case 2: return "Hand of Balance";
            case 3: return "Mind of Unity";
            default: return "Unknown Ability";
        }
    }
    
    
    // Helper method to create fallback icons if loading fails
    private void createFallbackIcon(int phase, boolean isActive) {
        String iconText;
        Color bgColor;

        // Determine icon text and color based on phase
        if (phase == 1) {
            iconText = "Eye";
            bgColor = isActive ? new Color(60, 150, 200) : Color.GRAY; // Blue for Eye
        } else if (phase == 2) {
            iconText = "Hand";
            bgColor = isActive ? new Color(200, 80, 160) : Color.GRAY; // Purple for Hand
        } else {
            iconText = "Mind";
            bgColor = isActive ? new Color(80, 200, 80) : Color.GRAY; // Green for Mind
        }

        // Create fallback image
        BufferedImage fallbackImg = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fallbackImg.createGraphics();
        g2d.setColor(bgColor);
        g2d.fillOval(15, 15, 120, 120);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.drawString(iconText, 45, 85);
        g2d.dispose();

        // Assign to the appropriate icon
        if (isActive) {
            currentActiveIcon = new ImageIcon(fallbackImg);
        } else {
            currentDisabledIcon = new ImageIcon(fallbackImg);
        }

        System.out.println("DEBUG: Created fallback icon for phase " + phase);
    }
    
    

    
    /**
     * Initialize all UI components
     */

    private void initializeUI() {
        // Keep the hint and pause buttons
        hintButton = createImageButton(hintNormalIcon, hintHoverIcon);
        hintButton.setBounds(GameConstants.WINDOW_WIDTH - 180, 20, 70, 70);
        hintButton.addActionListener(e -> showHint());
        add(hintButton);

        pauseButton = createImageButton(pauseNormalIcon, pauseHoverIcon);
        pauseButton.setBounds(GameConstants.WINDOW_WIDTH - 90, 20, 70, 70);
        pauseButton.addActionListener(e -> showPauseMenu());
        add(pauseButton);

        // Main heading/phase display
        phaseLabel = new JLabel("Phase 1: The Eye of Pattern", JLabel.CENTER);
        phaseLabel.setFont(pixelifySansFont.deriveFont(24f));
        phaseLabel.setForeground(Color.WHITE);
        phaseLabel.setBounds(0, 55, GameConstants.WINDOW_WIDTH, 40);
        add(phaseLabel);

        // Instructions
        instructionLabel = new JLabel(
            "Use your 'Eye of Pattern' ability to identify ingredient sequences (runs).", 
            JLabel.CENTER
        );
        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        instructionLabel.setForeground(Color.WHITE);
        instructionLabel.setBounds(0, 95, GameConstants.WINDOW_WIDTH, 30);
        add(instructionLabel);

        // FIXED GRID LAYOUT - Modified to be transparent for Phase 2
        // The grid should be a perfect 5x4 grid
        int GRID_PADDING = 12; // Border padding around grid
        gridPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Only draw background if we're in Phase 1
                if (currentPhase == 1) {
                    // Draw background image - rest remains the same
                    ImageIcon gridBgImage = resourceManager.getImage("/gameproject/resources/grid_bg.png");
                    if (gridBgImage != null) {
                        g.drawImage(gridBgImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                    } else {
                        g.setColor(new Color(165, 120, 95));
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
                // For Phase 2 and Phase 3, no background will be drawn
            }
        };

        // Calculate exact measurements for grid layout
        int cellSize = INGREDIENT_SIZE; // Each ingredient cell size
        int gridWidth = GRID_COLS * cellSize; 
        int gridHeight = GRID_ROWS * cellSize;

        // Add padding for the border/frame
        int totalWidth = gridWidth + (GRID_PADDING * 2);
        int totalHeight = gridHeight + (GRID_PADDING * 2);

        // Center the grid on screen and position it vertically
        int gridX = (GameConstants.WINDOW_WIDTH - totalWidth) / 2;
        int gridY = 135;

        // Position the grid panel including the border padding
        gridPanel.setBounds(gridX, gridY, totalWidth, totalHeight);
        gridPanel.setLayout(null); // Use absolute positioning
        gridPanel.setOpaque(false);
        add(gridPanel);

        // Control panel - keep position at bottom
        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBounds(0, GameConstants.WINDOW_HEIGHT - 100, GameConstants.WINDOW_WIDTH, 80);
        controlPanel.setOpaque(false);   

         // Ability button - KEEP IT but make it invisible
        abilityButton = createStyledButton("Use Eye of Pattern");
        abilityButton.addActionListener(e -> useAbility());


        // Check button
        checkButton = new AnimatedButton("Check Selection", 
            resourceManager.getImage("/gameproject/resources/NormalButton.png"),
            resourceManager.getImage("/gameproject/resources/HoverButton.png"),
            resourceManager.getImage("/gameproject/resources/ClickedButton.png"));
        Font buttonFont = resourceManager.getFont("/gameproject/resources/PixelifySans.ttf", 18f);
            if (buttonFont != null) {
                checkButton.setFont(buttonFont);
            } else {
                checkButton.setFont(new Font("Arial", Font.BOLD, 12));
            }
            checkButton.setForeground(Color.WHITE);
            checkButton.setMaximumSize(new Dimension(180, 40));
            checkButton.setPreferredSize(new Dimension(180, 40));
            checkButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        
        checkButton.addActionListener(e -> checkPhaseCompletion());
        checkButton.setEnabled(false);
        controlPanel.add(checkButton);

        add(controlPanel);


        // Create custom ability button panel in the lower left
        customAbilityButtonPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        customAbilityButtonPanel.setOpaque(false);

        customAbilityButtonPanel.setBounds(20, GameConstants.WINDOW_HEIGHT - 240, 200, 200);

        // Create icon label for the button with INCREASED SIZE
        abilityIconLabel = new JLabel(eyeActiveIcon);
        abilityIconLabel.setBounds(25, 0, 150, 150); // Centered within the 200px wide panel
        abilityIconLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        abilityIconLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("DEBUG: Ability icon clicked");
                if (abilityButtonEnabled) {
                    useAbility();
                }
            }
        });

        customAbilityButtonPanel.add(abilityIconLabel);

        // Create label for the ability name - REPOSITIONED for larger icon
        abilityNameLabel = new JLabel("Eye of Pattern", JLabel.CENTER);
        Font abilityFont = resourceManager.getFont("/gameproject/resources/PixelifySans.ttf", 18f); // Slightly larger font
        if (abilityFont != null) {
            abilityNameLabel.setFont(abilityFont);
        } else {
            abilityNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        }
        abilityNameLabel.setForeground(Color.WHITE);
        // Adjust position to be centered under the larger icon
        abilityNameLabel.setBounds(0, 160, 200, 30); // Wider and positioned under the image
        abilityNameLabel.setBorder(null);

        customAbilityButtonPanel.add(abilityNameLabel);

        // Important: Add the custom panel LAST to ensure it appears on top
        add(customAbilityButtonPanel);

        // Debug text to verify panel was added
        System.out.println("DEBUG: Custom ability button panel added at " + 
                           customAbilityButtonPanel.getBounds().toString());
    }

    // Override the setEnabled method for abilityButton to also update our custom button
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        // Update our custom button state
        setAbilityButtonEnabled(enabled);
    }

    
    
    
    
    // Add a method to enable/disable the ability button
    public void setAbilityButtonEnabled(boolean enabled) {
        this.abilityButtonEnabled = enabled;

        // Keep the original button's enabled state in sync
        if (abilityButton != null) {
            abilityButton.setEnabled(enabled);
        }

        if (abilityIconLabel != null) {
            System.out.println("DEBUG: Setting ability icon enabled: " + enabled);
            if (enabled) {
                abilityIconLabel.setIcon(currentActiveIcon);
                abilityIconLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                abilityIconLabel.setIcon(currentDisabledIcon);
                abilityIconLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    
    
    
    
    // Add a method to update the ability name text for different phases
    public void updateAbilityButtonText(String text) {
        // Update the original button text
        if (abilityButton != null) {
            abilityButton.setText(text);
        }

        // Extract just the ability name (without "Use")
        String abilityName = text.replace("Use ", "");

        // Update our custom button text
        if (abilityNameLabel != null) {
            abilityNameLabel.setText(abilityName);
            System.out.println("DEBUG: Updated ability name label to: " + abilityName);
        }
    }

    
    
    
    /**
    * Set the current game level (1, 2, or 3)
    */
    public void setGameLevel(int level) {
        this.gameLevel = level;
        System.out.println("DEBUG: TimSortVisualization game level set to: " + this.gameLevel);

        // Load the appropriate background
        loadBackground(currentPhase);

        // Update boss-specific elements
        if (level == 3) {
            // Level 3 - Lord Chaosa
            currentBossName = "Lord_Chaosa";

            // Update hints for Lord Chaosa
            String[] lordChaosaHints = new String[] {
                "Look for ingredients with orange and brown coloring that enhance raw physical power.",
                "Sort each group from foundational to peak. The proper sequence builds power progressively.",
                "Against reality-warping chaos, only overwhelming physical force can break through the distortion."
            };

            // Store the hints
            this.lordChaosaHints = lordChaosaHints;

            // Update phase label with Level 3 context
            if (currentPhase == 1) {
                phaseLabel.setText("Phase 1: The Eye of Pattern (Level 3 - Lord Chaosa)");
                instructionLabel.setText("Identify strength-enhancing ingredients to counter Lord Chaosa's reality distortions.");
            } else if (currentPhase == 2) {
                phaseLabel.setText("Phase 2: The Hand of Balance (Level 3 - Lord Chaosa)");
                instructionLabel.setText("Sort the ingredients to craft potions of raw strength.");
            } else if (currentPhase == 3) {
                phaseLabel.setText("Phase 3: The Mind of Unity (Level 3 - Lord Chaosa)");
                instructionLabel.setText("Choose which potion will be most effective against Lord Chaosa's reality-warping powers.");
            }
        } else if (level == 2) {
            // Level 2 - Toxitar
            currentBossName = "Toxitar";

            // Update hints for Toxitar
            toxitarHints = new String[] {
                "Look for ingredients with green coloring and light properties - these enhance agility.",
                "Sort each group from lightest to heaviest. The proper sequence is crucial for dexterity potions.",
                "Against poison that fills the air, quick movement is better than raw strength."
            };

            // Update phase label with Level 2 context
            if (currentPhase == 1) {
                phaseLabel.setText("Phase 1: The Eye of Pattern (Level 2 - Toxitar)");
                instructionLabel.setText("Identify agility-enhancing ingredients to evade Toxitar's poison.");
            } else if (currentPhase == 2) {
                phaseLabel.setText("Phase 2: The Hand of Balance (Level 2 - Toxitar)");
                instructionLabel.setText("Sort the ingredients to craft dexterity-enhancing potions.");
            } else if (currentPhase == 3) {
                phaseLabel.setText("Phase 3: The Mind of Unity (Level 2 - Toxitar)");
                instructionLabel.setText("Choose which potion will be most effective against Toxitar's poison.");
            }
        } else {
            // Default to Level 1 - Flameclaw
            currentBossName = "Flameclaw";

            // Update phase label with Level 1 context
            if (currentPhase == 1) {
                phaseLabel.setText("Phase 1: The Eye of Pattern");
                instructionLabel.setText("Use your 'Eye of Pattern' ability to identify ingredient sequences (runs).");
            } else if (currentPhase == 2) {
                phaseLabel.setText("Phase 2: The Hand of Balance");
                instructionLabel.setText("Use your 'Hand of Balance' to sort ingredients into two groups.");
            } else if (currentPhase == 3) {
                phaseLabel.setText("Phase 3: The Mind of Unity");
                instructionLabel.setText("Use your 'Mind of Unity' to choose which potion to craft.");
            }
        }

        // CRITICAL FIX: Make sure ability button is enabled for this level
        setAbilityButtonEnabled(true);
    }

    
    
    
    
    
    /**
    * Get the appropriate hint based on game level and phase
    */
    private String getLevelSpecificHint(int phase) {
        if (gameLevel == 3) {
            // Lord Chaosa hints
            if (phase >= 1 && phase <= 3) {
                return lordChaosaHints[phase - 1];
            }
        } else if (gameLevel == 2) {
            // Toxitar hints (existing code)
            if (phase >= 1 && phase <= 3) {
                return toxitarHints[phase - 1];
            }
        } else {
            // Flameclaw hints (existing code)
            if (phase == 1) {
                return "Look for ingredients with similar properties. Blue ingredients enhance movement!";
            } else if (phase == 2) {
                return "Sort each group from lowest to highest value. The order is crucial for potion effectiveness.";
            } else if (phase == 3) {
                return "Against Flameclaw's fire, consider what would protect you rather than what would make you powerful.";
            }
        }

        // Default hint if no specific one is available
        return "Observe the natural patterns in the ingredients and follow the guidance of the characters.";
    }
    
    
    
    
    /**
    * Start a boss battle with the appropriate boss based on game level
    */
    private void startBossBattle() {
        String bossName = "";
        // Make sure we're using the correct boss for each level
        switch (gameLevel) {
            case 1:
                bossName = "Flameclaw";
                break;
            case 2:
                bossName = "Toxitar";
                break;
            case 3:
                bossName = "LordChaosa";
                break;
            default:
                bossName = "Flameclaw"; // Default fallback
        }

        // Add debug to verify correct boss name is being used
        System.out.println("DEBUG: Starting boss battle with boss: " + bossName + " for game level: " + gameLevel);

        // CRITICAL FIX: Completely remove all UI elements that might persist
        gridPanel.removeAll();
        gridPanel.setVisible(false);

        startBossBattle(bossName);
    }
    
    
    
    
    
    /**
     * Create a button with normal and hover images
     */
    private JButton createImageButton(ImageIcon normalIcon, ImageIcon hoverIcon) {
        JButton button = new JButton(normalIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (hoverIcon != null) {
                    button.setIcon(hoverIcon);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (normalIcon != null) {
                    button.setIcon(normalIcon);
                }
            }
        });
        
        return button;
    }
    
    /**
    * Display pause menu overlay with semi-transparent dark background
    */
    private void showPauseMenu() {
        // Pause the timer

        // Create semi-transparent dark overlay panel
        JPanel overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Semi-transparent dark overlay (60% opacity black)
                g.setColor(new Color(0, 0, 0, 153)); // 153 is ~60% opacity
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlay.setLayout(null);
        overlay.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        overlay.setOpaque(false);
        add(overlay, 0);

        // Create menu container panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(null);
        menuPanel.setBounds(GameConstants.WINDOW_WIDTH / 2 - 150, GameConstants.WINDOW_HEIGHT / 2 - 150, 300, 300);

        // Use AnimatedButton class
        AnimatedButton resumeButton = new AnimatedButton("RESUME GAME", 
            resourceManager.getImage("/gameproject/resources/NormalButton.png"),
            resourceManager.getImage("/gameproject/resources/HoverButton.png"),
            resourceManager.getImage("/gameproject/resources/ClickedButton.png"));
        resumeButton.setFont(pixelifySansFont.deriveFont(28f));
        resumeButton.setForeground(Color.WHITE);
        resumeButton.setMaximumSize(new Dimension(300, 70));
        resumeButton.setPreferredSize(new Dimension(300, 70));
        resumeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resumeButton.addActionListener(e -> {
            remove(overlay);
            repaint();

        });

        AnimatedButton restartButton = new AnimatedButton("RESTART", 
            resourceManager.getImage("/gameproject/resources/NormalButton.png"),
            resourceManager.getImage("/gameproject/resources/HoverButton.png"),
            resourceManager.getImage("/gameproject/resources/ClickedButton.png"));
        restartButton.setFont(pixelifySansFont.deriveFont(28f));
        restartButton.setForeground(Color.WHITE);
        restartButton.setMaximumSize(new Dimension(300, 70));
        restartButton.setPreferredSize(new Dimension(300, 70));
        restartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartButton.addActionListener(e -> {
            // Show confirmation dialog before restarting
            int response = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to restart this level from the beginning?\nAny progress in this level will be lost.",
                "Confirm Restart",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (response == JOptionPane.YES_OPTION) {
                // Remove overlay
                remove(overlay);

                // Get the current game level
                int currentGameLevel = gameLevel;

                // Reset all phases for the current level
                resetAllPhases();

                // Maintain the current game level
                setGameLevel(currentGameLevel);

                // Reset phase to 1
                currentPhase = 1;

                // Signal controller to restart the current level from the beginning
                if (currentGameLevel == 1) {
                    controller.startGame(); // This starts Level 1 from the prologue
                } else if (currentGameLevel == 2) {
                    controller.startLevel2FromSelection(); // This starts Level 2 from the beginning
                } else if (currentGameLevel == 3) {
                    controller.startLevel3FromSelection(); // This starts Level 3 from the beginning
                }
            } else {
                // User canceled restart, remove overlay and resume game
                remove(overlay);
                repaint();
            }
        });

        AnimatedButton menuButton = new AnimatedButton("MAIN MENU", 
            resourceManager.getImage("/gameproject/resources/NormalButton.png"),
            resourceManager.getImage("/gameproject/resources/HoverButton.png"),
            resourceManager.getImage("/gameproject/resources/ClickedButton.png"));
        menuButton.setFont(pixelifySansFont.deriveFont(28f));
        menuButton.setForeground(Color.WHITE);
        menuButton.setMaximumSize(new Dimension(300, 70));
        menuButton.setPreferredSize(new Dimension(300, 70));
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.addActionListener(e -> {
            // Show confirmation dialog
            int response = JOptionPane.showConfirmDialog(
                this,
                "Return to main menu? Your progress in the current level will be saved.",
                "Main Menu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (response == JOptionPane.YES_OPTION) {
                // Save current progress
                saveCurrentProgress();

                // Remove overlay
                remove(overlay);

                // Return to main menu
                controller.showMainMenu();
            } else {
                // User canceled, remove overlay and resume game
                remove(overlay);
                repaint();
            }
        });

        // Add buttons to menu panel with spacing
        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(resumeButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(restartButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(menuButton);
        menuPanel.add(Box.createVerticalGlue());

        overlay.add(menuPanel);
        revalidate();
        repaint();
    }
    
    
    
    
    /**
    * Save the current progress to be resumed later
    */
    private void saveCurrentProgress() {
        // Create a static map in GameController to store level progress
        if (controller.levelProgressMap == null) {
            controller.levelProgressMap = new HashMap<>();
        }

        // Create a progress data object with current level and phase
        LevelProgressData progressData = new LevelProgressData(gameLevel, currentPhase);

        // Add any other state you need to save (like selected ingredients, etc.)
        progressData.setLeftPotionType(leftGroupPotionType);
        progressData.setRightPotionType(rightGroupPotionType);

        // Store current progress for this level
        controller.levelProgressMap.put(gameLevel, progressData);

        System.out.println("DEBUG: Saved progress for level " + gameLevel + ", phase " + currentPhase);
    }

    


    /**
     * Generate ingredients for the grid
     */
    private void generateIngredients() {
        allIngredients.clear();
        selectedIngredients.clear();
        System.out.println("DEBUG: Generating ingredients for game level: " + gameLevel);

        // Clear the grid panel
        gridPanel.removeAll();
        if (gameLevel == 3) {
                // For Level 3 (Lord Chaosa), prioritize Strength and Cold Resistance ingredients

                // Strength Potion (values 11-15) with orange background - most important for Lord Chaosa
                String[] strengthIngredients = {"corn", "powdered_giant_insect", "troll_sweat", "powdered_minotaur_horn", "dragon_bone"};
                for (int i = 0; i < 5; i++) {
                    IngredientItem ingredient = new IngredientItem(i + 11, "orange");
                    ingredient.setIngredientName(strengthIngredients[i]);
                    ingredient.setPotionType("strength");
                    allIngredients.add(ingredient);
                }

                // Cold Resistance Potion (values 6-10) with blue background - second priority
                String[] coldIngredients = {"strawberries", "wasabi", "mint", "dragon_ice_glands", "ice_crystal"};
                for (int i = 0; i < 5; i++) {
                    IngredientItem ingredient = new IngredientItem(i + 6, "blue");
                    ingredient.setIngredientName(coldIngredients[i]);
                    ingredient.setPotionType("cold");
                    allIngredients.add(ingredient);
                }

                // Dexterity Potion (values 16-20) - less important for Lord Chaosa
                String[] dexterityIngredients = {"banana_leaf", "maple_sap", "powdered_jackalope_antlers", "griffon_feathers", "dragon_sinew"};
                for (int i = 0; i < 5; i++) {
                    IngredientItem ingredient = new IngredientItem(i + 16, "green");
                    ingredient.setIngredientName(dexterityIngredients[i]);
                    ingredient.setPotionType("dexterity");
                    allIngredients.add(ingredient);
                }

                // Fire Resistance Potion (values 1-5) - least important for Lord Chaosa
                String[] fireIngredients = {"pumpkin", "apples", "peppers", "dragon_fire_glands", "fire_crystal"};
                for (int i = 0; i < 5; i++) {
                    IngredientItem ingredient = new IngredientItem(i + 1, "red");
                    ingredient.setIngredientName(fireIngredients[i]);
                    ingredient.setPotionType("fire");
                    allIngredients.add(ingredient);
                }
        } else if (gameLevel == 2) {
            // For Level 2 (Toxitar), prioritize Dexterity and Strength ingredients

            // Dexterity Potion (values 16-20) with green background - most important for Toxitar
            String[] dexterityIngredients = {"banana_leaf", "maple_sap", "powdered_jackalope_antlers", "griffon_feathers", "dragon_sinew"};
            for (int i = 0; i < 5; i++) {
                IngredientItem ingredient = new IngredientItem(i + 16, "green");
                ingredient.setIngredientName(dexterityIngredients[i]);
                ingredient.setPotionType("dexterity");
                allIngredients.add(ingredient);
            }

            // Strength Potion (values 11-15) with yellow background - second priority
            String[] strengthIngredients = {"corn", "powdered_giant_insect", "troll_sweat", "powdered_minotaur_horn", "dragon_bone"};
            for (int i = 0; i < 5; i++) {
                IngredientItem ingredient = new IngredientItem(i + 11, "yellow");
                ingredient.setIngredientName(strengthIngredients[i]);
                ingredient.setPotionType("strength");
                allIngredients.add(ingredient);
            }

            // Cold Resistance Potion (values 6-10) - less important for Toxitar
            String[] coldIngredients = {"strawberries", "wasabi", "mint", "dragon_ice_glands", "ice_crystal"};
            for (int i = 0; i < 5; i++) {
                IngredientItem ingredient = new IngredientItem(i + 6, "blue");
                ingredient.setIngredientName(coldIngredients[i]);
                ingredient.setPotionType("cold");
                allIngredients.add(ingredient);
            }

            // Fire Resistance Potion (values 1-5) - least important for Toxitar
            String[] fireIngredients = {"pumpkin", "apples", "peppers", "dragon_fire_glands", "fire_crystal"};
            for (int i = 0; i < 5; i++) {
                IngredientItem ingredient = new IngredientItem(i + 1, "red");
                ingredient.setIngredientName(fireIngredients[i]);
                ingredient.setPotionType("fire");
                allIngredients.add(ingredient);
            }
        } else {
            // For Level 1 (Flameclaw), prioritize Fire Resistance and Cold Resistance ingredients

            // Fire Resistance Potion (values 1-5) - most important for Flameclaw
            String[] fireIngredients = {"pumpkin", "apples", "peppers", "dragon_fire_glands", "fire_crystal"};
            for (int i = 0; i < 5; i++) {
                IngredientItem ingredient = new IngredientItem(i + 1, "red");
                ingredient.setIngredientName(fireIngredients[i]);
                ingredient.setPotionType("fire");
                allIngredients.add(ingredient);
            }

            // Cold Resistance Potion (values 6-10) - second priority
            String[] coldIngredients = {"strawberries", "wasabi", "mint", "dragon_ice_glands", "ice_crystal"};
            for (int i = 0; i < 5; i++) {
                IngredientItem ingredient = new IngredientItem(i + 6, "blue");
                ingredient.setIngredientName(coldIngredients[i]);
                ingredient.setPotionType("cold");
                allIngredients.add(ingredient);
            }

            // Strength Potion (values 11-15) - less important for Flameclaw
            String[] strengthIngredients = {"corn", "powdered_giant_insect", "troll_sweat", "powdered_minotaur_horn", "dragon_bone"};
            for (int i = 0; i < 5; i++) {
                IngredientItem ingredient = new IngredientItem(i + 11, "yellow");
                ingredient.setIngredientName(strengthIngredients[i]);
                ingredient.setPotionType("strength");
                allIngredients.add(ingredient);
            }

            // Dexterity Potion (values 16-20) - least important for Flameclaw
            String[] dexterityIngredients = {"banana_leaf", "maple_sap", "powdered_jackalope_antlers", "griffon_feathers", "dragon_sinew"};
            for (int i = 0; i < 5; i++) {
                IngredientItem ingredient = new IngredientItem(i + 16, "green");
                ingredient.setIngredientName(dexterityIngredients[i]);
                ingredient.setPotionType("dexterity");
                allIngredients.add(ingredient);
            }
        }

        // Randomly position ingredients in the grid
        Collections.shuffle(allIngredients);
        positionIngredientsInGrid();

        // Make grid boxes visible by default
        for (IngredientItem ingredient : allIngredients) {
            ingredient.setBoxVisible(true);
        }
        gridPanel.setVisible(true);
        System.out.println("DEBUG: Grid panel visibility: " + gridPanel.isVisible());
        // Repaint
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    
    /**
    * Position ingredients in the 4×5 grid
    */
    private void positionIngredientsInGrid() {
        // Define the grid padding (space between grid edge and first ingredients)
        int GRID_PADDING = 12;

        // ADDED: Check if gridPanel has correct dimensions before placing ingredients
        if (gridPanel.getWidth() != (GRID_COLS * INGREDIENT_SIZE) + (GRID_PADDING * 2) ||
            gridPanel.getHeight() != (GRID_ROWS * INGREDIENT_SIZE) + (GRID_PADDING * 2)) {

            // Reset grid panel dimensions if they've been changed
            int gridWidth = GRID_COLS * INGREDIENT_SIZE;
            int gridHeight = GRID_ROWS * INGREDIENT_SIZE;
            int totalWidth = gridWidth + (GRID_PADDING * 2);
            int totalHeight = gridHeight + (GRID_PADDING * 2);
            int gridX = (GameConstants.WINDOW_WIDTH - totalWidth) / 2;
            int gridY = 135;

            gridPanel.setBounds(gridX, gridY, totalWidth, totalHeight);
            System.out.println("DEBUG: Reset gridPanel dimensions to: " + gridX + "," + gridY + "," + totalWidth + "," + totalHeight);
        }

        for (int i = 0; i < allIngredients.size(); i++) {
            IngredientItem ingredient = allIngredients.get(i);

            // Calculate grid positions for 4×5 layout
            int row = i / GRID_COLS;
            int col = i % GRID_COLS;

            // Position in grid - add padding to align with background
            ingredient.setBounds(
                GRID_PADDING + (col * INGREDIENT_SIZE),
                GRID_PADDING + (row * INGREDIENT_SIZE),
                INGREDIENT_SIZE,
                INGREDIENT_SIZE
            );

            // Add to grid panel
            gridPanel.add(ingredient);

            // Add click listener
            ingredient.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleIngredientClick(ingredient);
                }
            });
        }
    }

  
    
    /**
     * Handle ingredient click based on current phase
     */
    private void handleIngredientClick(IngredientItem ingredient) {
        if (currentPhase == 1) {
            // Phase 1: Select/deselect ingredients
            if (ingredient.isSelected()) {
                // Deselect
                ingredient.setSelected(false);
                selectedIngredients.remove(ingredient);
            } else if (selectedIngredients.size() < MAX_SELECTIONS) {
                // Select only if under limit
                ingredient.setSelected(true);
                selectedIngredients.add(ingredient);
            }
            
            // Update UI
            checkButton.setEnabled(selectedIngredients.size() == MAX_SELECTIONS);
            
        } else if (currentPhase == 2) {
            // Phase 1: Select/deselect ingredients
            if (ingredient.isSelected()) {
                // Deselect
                ingredient.setSelected(false);
                selectedIngredients.remove(ingredient);
            } else if (selectedIngredients.size() < MAX_SELECTIONS) {
                // Select only if under limit
                ingredient.setSelected(true);
                selectedIngredients.add(ingredient);
            }
            
            // Update UI
            checkButton.setEnabled(selectedIngredients.size() == MAX_SELECTIONS);            
        } else if (currentPhase == 3) {
            // Phase 3: Choose between potions
            if (phaseCompleted) {
                return;
            }
            
            // Check if clicking on a group label
            if (ingredient.isGroupLabel()) {
                selectPotionGroup(ingredient.getValue());
            }
        }
        
        repaint();
    }
    
    
    
    /**
    * Improved sort group method that does a thorough sort
    */
    private void sortGroup(List<IngredientItem> group) {
        // Sort using a simple and reliable approach - Java's built-in sort
        Collections.sort(group, (a, b) -> Integer.compare(a.getValue(), b.getValue()));
    }
    
    
    /**
    * Select a potion group in Phase 3
    */
    private void selectPotionGroup(int groupId) {
        // Debug which potion is being selected
        System.out.println("DEBUG: Selecting potion group " + groupId);

        // Use potion types from Phase 2 to set the craftedPotion variable
        if (groupId == 1) {
            craftedPotion = leftGroupPotionType + " Potion";
            System.out.println("DEBUG: Selected left potion: " + craftedPotion);
        } else {
            craftedPotion = rightGroupPotionType + " Potion";
            System.out.println("DEBUG: Selected right potion: " + craftedPotion);
        }

        // Generate boss-specific message based on game level
        String message = craftedPotion + " selected!";
        String bossName = "";

        if (gameLevel == 3) {
            bossName = "Lord Chaosa";

            if (craftedPotion.contains("Strength")) {
                message += "\n\nThis potion grants tremendous physical power, perfect for breaking through Lord Chaosa's reality distortions.";
            } else if (craftedPotion.contains("Cold")) {
                message += "\n\nThis potion provides protection against cold, but may not be effective against Lord Chaosa's reality-warping abilities.";
            } else if (craftedPotion.contains("Dexterity")) {
                message += "\n\nThis potion enhances agility, but Lord Chaosa's distortions cannot be evaded through speed alone.";
            } else if (craftedPotion.contains("Fire")) {
                message += "\n\nThis potion protects against fire, but Lord Chaosa's powers are not element-based.";
            }
        } else if (gameLevel == 2) {
            bossName = "Toxitar";

            if (craftedPotion.contains("Dexterity")) {
                message += "\n\nThis potion enhances your agility, perfect for avoiding Toxitar's poisonous clouds.";
            } else if (craftedPotion.contains("Strength")) {
                message += "\n\nThis potion enhances raw power, but may not help avoid Toxitar's spreading poison.";
            } else if (craftedPotion.contains("Cold")) {
                message += "\n\nThis potion protects against cold, but Toxitar's poison isn't temperature-based.";
            } else if (craftedPotion.contains("Fire")) {
                message += "\n\nThis potion protects against fire, but will not help against Toxitar's poison.";
            }
        } else {
            bossName = "Flameclaw";

            if (craftedPotion.contains("Fire")) {
                message += "\n\nThis potion provides protection against Flameclaw's intense flames.";
            } else if (craftedPotion.contains("Strength")) {
                message += "\n\nThis potion enhances physical power, but may not protect against Flameclaw's fiery attacks.";
            } else if (craftedPotion.contains("Dexterity")) {
                message += "\n\nThis potion enhances agility, but Flameclaw's flames fill the area, making them hard to dodge.";
            } else if (craftedPotion.contains("Cold")) {
                message += "\n\nThis potion provides protection against cold, not heat. It may not be effective against Flameclaw.";
            }
        }

        // Show potion selection message
        JOptionPane.showMessageDialog(this, message, "Potion Selection", JOptionPane.INFORMATION_MESSAGE);

        // Enable check button
        checkButton.setEnabled(true);
        phaseCompleted = true;
    }

    /**
     * Use the current phase ability
     */
    private void useAbility() {
        // Check if button is enabled
        if (!abilityButtonEnabled) return;

        // Temporarily disable the button during animation
        setAbilityButtonEnabled(false);

        if (currentPhase == 1) {
            // Eye of Pattern - Highlight runs
            highlightNaturalRuns();

            // After a short delay, show the middle dialogue
            Timer dialogueTimer = new Timer(1500, e -> {
                // Select the appropriate dialogue based on current game level
                if (gameLevel == 3) {
                    showPhaseDialogue("level3_phase1_middle");
                } else if (gameLevel == 2) {
                    showPhaseDialogue("level2_phase1_middle");
                } else {
                    showPhaseDialogue("phase1_middle");
                }
            });
            dialogueTimer.setRepeats(false);
            dialogueTimer.start();
        } else if (currentPhase == 2) {
            // Hand of Balance - Apply automatic sorting
            applyHandOfBalanceAbility();

            // After sorting animation completes, show the middle dialogue
            Timer dialogueTimer = new Timer(3000, e -> {
                // Select the appropriate dialogue based on current game level
                if (gameLevel == 3) {
                    showPhaseDialogue("level3_phase2_middle");
                } else if (gameLevel == 2) {
                    showPhaseDialogue("level2_phase2_middle");
                } else {
                    showPhaseDialogue("phase2_middle");
                }
            });
            dialogueTimer.setRepeats(false);
            dialogueTimer.start();
        } else if (currentPhase == 3) {
            // Mind of Unity - Animate potion options display
            animatePotionOptions();

            // After animation completes, show the decision dialogue
            Timer dialogueTimer = new Timer(2500, e -> {
                // Select the appropriate dialogue based on current game level
                if (gameLevel == 3) {
                    showPhaseDialogue("level3_phase3_decision");
                } else if (gameLevel == 2) {
                    showPhaseDialogue("level2_phase3_decision");
                } else {
                    showPhaseDialogue("phase3_decision");
                }
            });
            dialogueTimer.setRepeats(false);
            dialogueTimer.start();
        }

        // Re-enable the button after a delay to prevent multiple clicks
        Timer enableTimer = new Timer(3000, e -> {
            setAbilityButtonEnabled(true);
        });
        enableTimer.setRepeats(false);
        enableTimer.start();

        // Add this timer to active timers list to manage it
        activeTimers.add(enableTimer);
    }    
    
    /**
    * Animate the potion options appearing without any backgrounds or overlays
    */
    private void animatePotionOptions() {
        // First, ensure ability button is enabled and in correct state before animation
        setAbilityButtonEnabled(true);
        System.out.println("DEBUG: Ability button enabled at start of animatePotionOptions");

        // Separate lists for different types of components
        List<JComponent> textElements = new ArrayList<>();     // Text labels & descriptions (no scaling)
        List<JComponent> imageElements = new ArrayList<>();    // Potion images (keep scaling)
        Map<JComponent, Rectangle> imageElementBounds = new HashMap<>(); // Original bounds for image elements

        // Classify all invisible components
        for (Component comp : gridPanel.getComponents()) {
            if (!comp.isVisible() && comp instanceof JComponent) {
                // Store original bounds
                Rectangle originalBounds = comp.getBounds();

                // Make component visible but initially transparent
                comp.setVisible(true);

                // Check component type to decide how to animate it
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;

                    // Check if this is an image label (potion bottle) or text label
                    if (label.getIcon() != null) {
                        // This is an image label (potion)
                        imageElements.add(label);
                        imageElementBounds.put(label, originalBounds);

                        // Set initial size (50% scale) for image while keeping center point
                        int centerX = originalBounds.x + originalBounds.width/2;
                        int centerY = originalBounds.y + originalBounds.height/2;
                        int initialWidth = originalBounds.width / 2;
                        int initialHeight = originalBounds.height / 2;
                        label.setBounds(centerX - initialWidth/2, centerY - initialHeight/2, initialWidth, initialHeight);
                    } else {
                        // This is a text label (titles, descriptions)
                        textElements.add(label);
                    }
                } 
                else if (comp instanceof JTextArea) {
                    // Text descriptions
                    textElements.add((JComponent)comp);
                }
                else if (comp instanceof JPanel) {
                    // For panels, set initial transparency to 0
                    ((JPanel) comp).setBackground(new Color(0f, 0f, 0f, 0f));
                    textElements.add((JComponent)comp);
                }
                else {
                    // Default to text treatment for any other component types
                    textElements.add((JComponent)comp);
                }
            }
        }

        // Create animation timer
        final int TOTAL_FRAMES = 50; // 2 seconds @ 25 fps
        final int[] currentFrame = {0};

        Timer animationTimer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentFrame[0]++;
                float progress = (float) currentFrame[0] / TOTAL_FRAMES;

                // Apply easing function for smooth animation
                float easedProgress;
                if (progress < 0.5) {
                    easedProgress = 4 * progress * progress * progress;
                } else {
                    float f = progress - 1;
                    easedProgress = 1 + 4 * f * f * f;
                }

                // Animate text elements (fade only, no scaling)
                for (JComponent comp : textElements) {
                    if (comp instanceof JPanel) {
                        float alpha = easedProgress;
                        ((JPanel) comp).setBackground(new Color(0f, 0f, 0f, alpha * 0.5f));
                    }
                    // For labels and other components, we'll use a trick with AlphaComposite
                    // But this happens naturally through parent component alpha handling
                }

                // Animate image elements (both fade AND scale)
                for (JComponent comp : imageElements) {
                    Rectangle origBounds = imageElementBounds.get(comp);
                    if (origBounds != null) {
                        int centerX = origBounds.x + origBounds.width/2;
                        int centerY = origBounds.y + origBounds.height/2;

                        // Calculate current size (50% to 100%)
                        float scale = 0.5f + (easedProgress * 0.5f);
                        int currentWidth = (int)(origBounds.width * scale);
                        int currentHeight = (int)(origBounds.height * scale);

                        // Position to keep centered during scaling
                        comp.setBounds(
                            centerX - currentWidth/2,
                            centerY - currentHeight/2,
                            currentWidth,
                            currentHeight
                        );
                    }
                }

                // Repaint panel
                gridPanel.repaint();

                // Stop animation when complete
                if (currentFrame[0] >= TOTAL_FRAMES) {
                    ((Timer)e.getSource()).stop();

                    // Restore exact original bounds for image elements
                    for (JComponent comp : imageElements) {
                        Rectangle origBounds = imageElementBounds.get(comp);
                        if (origBounds != null) {
                            comp.setBounds(origBounds);
                        }
                    }

                    // Set final transparency for panels
                    for (JComponent comp : textElements) {
                        if (comp instanceof JPanel) {
                            ((JPanel) comp).setBackground(new Color(0, 0, 0, 0.5f));
                        }
                    }

                    // Update instruction with dynamic boss reference
                    String bossReference;
                    if (gameLevel == 3) {
                        bossReference = "Lord Chaosa's reality distortions";
                    } else if (gameLevel == 2) {
                        bossReference = "Toxitar's poison";
                    } else {
                        bossReference = "Flameclaw";
                    }
                    instructionLabel.setText("Choose which potion to craft by clicking on it. Consider what would be most effective against " + bossReference + ".");

                    // CRITICAL FIX: Ensure ability button remains enabled after animation
                    setAbilityButtonEnabled(true);
                    System.out.println("DEBUG: Ability button enabled at end of animation");

                    // Force refresh
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }
            }
        });

        // Add to active timers list
        activeTimers.add(animationTimer);

        // Start animation
        animationTimer.start();
    }

    
    
    /**
     * Highlight natural runs in the ingredient grid
     */
    private void highlightNaturalRuns() {
        identifiedRuns.clear();

        if (gameLevel == 3) {
            // For Level 3 (Lord Chaosa), highlight strength ingredients the most prominently

            // Find runs for each potion type with adjusted colors for Level 3
            List<IngredientItem> strengthRun = findRunByPotionType("strength");
            if (!strengthRun.isEmpty()) {
                identifiedRuns.add(strengthRun);
                highlightRun(strengthRun, new Color(255, 140, 0, 120)); // Brighter orange highlight for strength (most important)
            }

            List<IngredientItem> coldRun = findRunByPotionType("cold");
            if (!coldRun.isEmpty()) {
                identifiedRuns.add(coldRun);
                highlightRun(coldRun, new Color(80, 80, 220, 100)); // Blue highlight for cold (secondary)
            }

            List<IngredientItem> dexterityRun = findRunByPotionType("dexterity");
            if (!dexterityRun.isEmpty()) {
                identifiedRuns.add(dexterityRun);
                highlightRun(dexterityRun, new Color(50, 200, 50, 60)); // Dimmer green highlight (less important)
            }

            List<IngredientItem> fireRun = findRunByPotionType("fire");
            if (!fireRun.isEmpty()) {
                identifiedRuns.add(fireRun);
                highlightRun(fireRun, new Color(255, 50, 50, 60)); // Dimmer red highlight (least important)
            }
        } else if (gameLevel == 2) {
            // For Level 2 (Toxitar), highlight dexterity ingredients the most prominently

            // Find runs for each potion type with adjusted colors for Level 2
            List<IngredientItem> dexterityRun = findRunByPotionType("dexterity");
            if (!dexterityRun.isEmpty()) {
                identifiedRuns.add(dexterityRun);
                highlightRun(dexterityRun, new Color(50, 200, 50, 120)); // Brighter green highlight for dexterity (most important)
            }

            List<IngredientItem> strengthRun = findRunByPotionType("strength");
            if (!strengthRun.isEmpty()) {
                identifiedRuns.add(strengthRun);
                highlightRun(strengthRun, new Color(255, 200, 50, 100)); // Yellow highlight for strength (secondary)
            }

            List<IngredientItem> coldRun = findRunByPotionType("cold");
            if (!coldRun.isEmpty()) {
                identifiedRuns.add(coldRun);
                highlightRun(coldRun, new Color(50, 50, 255, 60)); // Dimmer blue highlight (less important)
            }

            List<IngredientItem> fireRun = findRunByPotionType("fire");
            if (!fireRun.isEmpty()) {
                identifiedRuns.add(fireRun);
                highlightRun(fireRun, new Color(255, 50, 50, 60)); // Dimmer red highlight (least important)
            }
        } else {
            // For Level 1 (Flameclaw), highlight fire resistance ingredients the most prominently

            // Find runs for each potion type with corrected colors
            List<IngredientItem> fireRun = findRunByPotionType("fire");
            if (!fireRun.isEmpty()) {
                identifiedRuns.add(fireRun);
                highlightRun(fireRun, new Color(255, 50, 50, 120)); // Brighter red highlight for fire (most important)
            }

            List<IngredientItem> coldRun = findRunByPotionType("cold");
            if (!coldRun.isEmpty()) {
                identifiedRuns.add(coldRun);
                highlightRun(coldRun, new Color(50, 50, 255, 100)); // Blue highlight for cold (secondary)
            }

            List<IngredientItem> strengthRun = findRunByPotionType("strength");
            if (!strengthRun.isEmpty()) {
                identifiedRuns.add(strengthRun);
                highlightRun(strengthRun, new Color(255, 200, 50, 60)); // Dimmer yellow highlight (less important)
            }

            List<IngredientItem> dexterityRun = findRunByPotionType("dexterity");
            if (!dexterityRun.isEmpty()) {
                identifiedRuns.add(dexterityRun);
                highlightRun(dexterityRun, new Color(50, 200, 50, 60)); // Dimmer green highlight (least important)
            }
        }

        // Update the instruction
        if (gameLevel == 3) {
            instructionLabel.setText("Natural runs highlighted! Select exactly 10 ingredients that form sequences. Orange ingredients enhance strength!");
        } else if (gameLevel == 2) {
            instructionLabel.setText("Natural runs highlighted! Select exactly 10 ingredients that form sequences. Green ingredients enhance agility!");
        } else {
            instructionLabel.setText("Natural runs highlighted! Select exactly 10 ingredients that form sequences. Blue ingredients resist fire!");
        }

        // Enable check button if exactly 10 ingredients are selected
        checkButton.setEnabled(selectedIngredients.size() == MAX_SELECTIONS);
    }
    
    /**
     * Find a run of ingredients by color
     */
    private List<IngredientItem> findRunByPotionType(String potionType) {
        List<IngredientItem> run = new ArrayList<>();

        // Get all ingredients of this potion type
        for (IngredientItem ingredient : allIngredients) {
            if (ingredient.getPotionType().equals(potionType)) {
                run.add(ingredient);
            }
        }

        // Sort by value to ensure they're in the correct order
        run.sort((a, b) -> Integer.compare(a.getValue(), b.getValue()));

        return run;
    }
    
    /**
     * Highlight a run of ingredients
     */
    private void highlightRun(List<IngredientItem> run, Color highlightColor) {
            for (IngredientItem ingredient : run) {
                ingredient.setHighlighted(true);
                ingredient.setHighlightColor(highlightColor);
            }
        }

    /**
    * Arrange ingredients for sorting in Phase 2
    */
    private void arrangeIngredientsForSorting() {
        // Clear groups
        leftGroup.clear();
        rightGroup.clear();

        // Clear the grid panel
        gridPanel.removeAll();

        // Make sure grid panel has no background
        gridPanel.setBackground(new Color(0, 0, 0, 0));
        gridPanel.setOpaque(false);

        // Make grid panel cover the entire game area
        gridPanel.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);

        // Update ability button text and instruction
        abilityButton.setText("Use Hand of Balance");
        instructionLabel.setText("Use your 'Hand of Balance' to sort ingredients into two groups.");

        checkButton.setEnabled(false);

        // Check if we have selected ingredients from Phase 1
        if (selectedIngredients.isEmpty() && currentPhase == 2) {
            // If no ingredients were selected in Phase 1, generate backup ingredients
            createBackupIngredients();
        }

        // Create a list of ingredients to use in Phase 2
        List<IngredientItem> ingredientsToUse = new ArrayList<>();

        // Get up to 10 ingredients from Phase 1 selection
        int numToTake = Math.min(selectedIngredients.size(), 10);
        for (int i = 0; i < numToTake; i++) {
            IngredientItem originalItem = selectedIngredients.get(i);
            IngredientItem newItem = new IngredientItem(originalItem.getValue(), originalItem.getColor());

            // Copy important properties
            newItem.setPotionType(originalItem.getPotionType());
            newItem.setIngredientName(originalItem.getIngredientName());

            // Ensure box is not visible for Phase 2
            newItem.setBoxVisible(false);

            ingredientsToUse.add(newItem);
        }

        // If we didn't get 10, add from our backup
        if (ingredientsToUse.size() < 10 && allIngredients.size() >= 10) {
            for (int i = 0; i < 10 - ingredientsToUse.size(); i++) {
                if (i < allIngredients.size()) {
                    IngredientItem originalItem = allIngredients.get(i);
                    IngredientItem newItem = new IngredientItem(originalItem.getValue(), originalItem.getColor());
                    newItem.setPotionType(originalItem.getPotionType());
                    newItem.setIngredientName(originalItem.getIngredientName());
                    newItem.setBoxVisible(false);
                    ingredientsToUse.add(newItem);
                }
            }
        }

        // Define safe areas to keep ingredients visible
        int safeTop = 150;    // Stay below the title
        int safeBottom = 600; // Stay above the buttons
        int safeLeft = 50;    // Stay away from left edge
        int safeRight = 950;  // Stay away from right edge

        // Create randomized positions
        Random random = new Random();
        List<Point> randomPositions = new ArrayList<>();

        // Generate random positions for all ingredients ensuring they don't overlap
        while (randomPositions.size() < ingredientsToUse.size()) {
            // Generate random position within safe area
            int x = safeLeft + random.nextInt(safeRight - safeLeft - INGREDIENT_SIZE);
            int y = safeTop + random.nextInt(safeBottom - safeTop - INGREDIENT_SIZE);
            Point newPos = new Point(x, y);

            // Check if this position overlaps with any existing positions
            boolean overlaps = false;
            for (Point existingPos : randomPositions) {
                // Consider overlap if distance is less than 1.5x ingredient size
                if (existingPos.distance(newPos) < INGREDIENT_SIZE * 1.1) {
                    overlaps = true;
                    break;
                }
            }

            // Only add non-overlapping positions
            if (!overlaps) {
                randomPositions.add(newPos);
            }
        }

        // Group ingredients by potion type to help balance the distribution
        List<IngredientItem> fireIngredients = new ArrayList<>();
        List<IngredientItem> coldIngredients = new ArrayList<>();
        List<IngredientItem> strengthIngredients = new ArrayList<>();
        List<IngredientItem> dexterityIngredients = new ArrayList<>();

        // Group by type
        for (IngredientItem ingredient : ingredientsToUse) {
            String potionType = ingredient.getPotionType();
            if (potionType != null) {
                if (potionType.equals("fire")) {
                    fireIngredients.add(ingredient);
                } else if (potionType.equals("cold")) {
                    coldIngredients.add(ingredient);
                } else if (potionType.equals("strength")) {
                    strengthIngredients.add(ingredient);
                } else if (potionType.equals("dexterity")) {
                    dexterityIngredients.add(ingredient);
                }
            }
        }

        // Combine all ingredient types with the most common types first
        List<IngredientItem> allSorted = new ArrayList<>();

        // Sort the lists by size (largest first) to prioritize the most common ingredient types
        List<List<IngredientItem>> allTypeLists = new ArrayList<>();
        allTypeLists.add(fireIngredients);
        allTypeLists.add(coldIngredients);
        allTypeLists.add(strengthIngredients);
        allTypeLists.add(dexterityIngredients);

        // Sort by size (descending)
        Collections.sort(allTypeLists, (a, b) -> Integer.compare(b.size(), a.size()));

        // Add to the final list in order of frequency
        for (List<IngredientItem> typeList : allTypeLists) {
            allSorted.addAll(typeList);
        }

        // In case we have less than 10 total ingredients
        int count = Math.min(10, allSorted.size());

        // Apply sized increase (15 pixels as requested)
        int sizeIncrease = 15;

        // Assign ingredients to left and right groups
        for (int i = 0; i < count; i++) {
            IngredientItem ingredient = allSorted.get(i);

            // Reset visual state
            ingredient.setSelected(false);
            ingredient.setHighlighted(false);

            // Increase size slightly
            ingredient.setSize(INGREDIENT_SIZE + sizeIncrease, INGREDIENT_SIZE + sizeIncrease);

            // Get position from our random positions list
            Point pos = randomPositions.get(i);

            // Store original position for animation
            ingredient.setOriginalPosition(pos);
            ingredient.setLocation(pos.x, pos.y);

            // Add to appropriate group based on the potion type to create sensible groupings
            // Distribute evenly - first half to left group, second half to right group
            if (i < count / 2) {
                leftGroup.add(ingredient);
            } else {
                rightGroup.add(ingredient);
            }

            // Add to grid panel
            gridPanel.add(ingredient);

            // Add click listeners for item interaction
            ingredient.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleIngredientClick(ingredient);
                }
            });

            // Make items always selectable for sorting
            ingredient.setSelected(true);
        }

        // Enable the ability button
        abilityButton.setEnabled(true);
        setAbilityButtonEnabled(true);

        // Make sure grid panel is on top of other components
        if (getComponentZOrder(gridPanel) == 0) {
            setComponentZOrder(gridPanel, getComponentCount() - 1);
        }

        // Force UI refresh
        gridPanel.revalidate();
        gridPanel.repaint();
    }
    
    /**
    * Create backup ingredients if needed
    */
    private void createBackupIngredients() {
        // Clear any existing ingredients to prevent duplicates
        selectedIngredients.clear();

        // Create a new set of ingredients for Phase 2
        String[] colors = {"red", "blue", "green", "yellow", "orange"};

        // Create 10 ingredients with values 1-10
        for (int i = 0; i < 10; i++) {
            String color = colors[i % colors.length];
            IngredientItem ingredient = new IngredientItem(i + 1, color);

            // Set proper names based on index
            if (i < 5) {
                // Fire resistance ingredients
                String[] fireIngredients = {"pumpkin", "apples", "peppers", "dragon_fire_glands", "fire_crystal"};
                ingredient.setIngredientName(fireIngredients[i]);
                ingredient.setPotionType("fire");
            } else {
                // Strength ingredients
                String[] strengthIngredients = {"corn", "powdered_giant_insect", "troll_sweat", "powdered_minotaur_horn", "dragon_bone"};
                ingredient.setIngredientName(strengthIngredients[i - 5]);
                ingredient.setPotionType("strength");
            }

            selectedIngredients.add(ingredient);
        }
    }
    
    /**
    * Determine the potion type based on the ingredients in a group
    */
    private String determinePotionType(List<IngredientItem> group) {
        // Count occurrences of each potion type
        int fireCount = 0;
        int coldCount = 0;
        int strengthCount = 0;
        int dexterityCount = 0;

        for (IngredientItem item : group) {
            String potionType = item.getPotionType();
            if (potionType != null) {
                if (potionType.equals("fire")) {
                    fireCount++;
                } else if (potionType.equals("cold")) {
                    coldCount++;
                } else if (potionType.equals("strength")) {
                    strengthCount++;
                } else if (potionType.equals("dexterity")) {
                    dexterityCount++;
                }
            }
        }

        // Determine the dominant potion type
        int maxCount = Math.max(Math.max(fireCount, coldCount), Math.max(strengthCount, dexterityCount));

        if (maxCount == fireCount && fireCount > 0) {
            return "Fire Resistance";
        } else if (maxCount == coldCount && coldCount > 0) {
            return "Cold Resistance";
        } else if (maxCount == strengthCount && strengthCount > 0) {
            return "Strength";
        } else if (maxCount == dexterityCount && dexterityCount > 0) {
            return "Dexterity";
        } else {
            return "Mixed"; // Fallback if no clear type or empty group
        }
    }
    
    
    
    
    
    /**
    * Apply the Hand of Balance ability to sort ingredients
    */
    private void applyHandOfBalanceAbility() {   
        // Determine potion types based on the ingredients in each group
        leftGroupPotionType = determinePotionType(leftGroup);
        rightGroupPotionType = determinePotionType(rightGroup);

        // First, sort both groups using our existing method
        sortGroup(leftGroup);
        sortGroup(rightGroup);

        // Now verify the sorting is correct using the isGroupSorted method
        isLeftGroupSorted = isGroupSorted(leftGroup);
        isRightGroupSorted = isGroupSorted(rightGroup);

        // If either group is not properly sorted, log a warning
        if (!isLeftGroupSorted || !isRightGroupSorted) {
            System.err.println("WARNING: Group sorting failed - Left sorted: " + 
                              isLeftGroupSorted + ", Right sorted: " + isRightGroupSorted);

            // Attempt re-sort if needed
            if (!isLeftGroupSorted) {
                System.err.println("Attempting to resort left group");
                Collections.sort(leftGroup, (a, b) -> Integer.compare(a.getValue(), b.getValue()));
                isLeftGroupSorted = true;
            }

            if (!isRightGroupSorted) {
                System.err.println("Attempting to resort right group");
                Collections.sort(rightGroup, (a, b) -> Integer.compare(a.getValue(), b.getValue()));
                isRightGroupSorted = true;
            }
        }

        // Determine potion types based on the ingredients in each group
        leftGroupPotionType = determinePotionType(leftGroup);
        rightGroupPotionType = determinePotionType(rightGroup);

        // Clear any existing headers
        for (Component c : gridPanel.getComponents()) {
            if (c instanceof JLabel && !(c instanceof IngredientItem)) {
                gridPanel.remove(c);
            }
        }

        // Create headers with potion types
        JLabel topHeader = new JLabel(leftGroupPotionType + " Ingredients", JLabel.CENTER);
        topHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        topHeader.setForeground(getColorForPotionType(leftGroupPotionType));
        topHeader.setBounds(0, 180, GameConstants.WINDOW_WIDTH, 30);
        topHeader.setVisible(true);
        gridPanel.add(topHeader);

        JLabel bottomHeader = new JLabel(rightGroupPotionType + " Ingredients", JLabel.CENTER);
        bottomHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        bottomHeader.setForeground(getColorForPotionType(rightGroupPotionType));
        bottomHeader.setBounds(0, 350, GameConstants.WINDOW_WIDTH, 30);
        bottomHeader.setVisible(true);
        gridPanel.add(bottomHeader);

        // Disable the ability button during animation
        abilityButton.setEnabled(false);

        // Update boss-specific headers for Level 3
        if (gameLevel == 3) {
            // Set potion type relevant to Lord Chaosa
            // For Level 3, prefer Strength and Cold Resistance potions
            if (!leftGroupPotionType.equals("Strength") && !leftGroupPotionType.equals("Cold Resistance")) {
                if (leftGroup.stream().anyMatch(item -> item.getPotionType().equals("strength"))) {
                    leftGroupPotionType = "Strength";
                } else if (leftGroup.stream().anyMatch(item -> item.getPotionType().equals("cold"))) {
                    leftGroupPotionType = "Cold Resistance";
                }
            }

            if (!rightGroupPotionType.equals("Strength") && !rightGroupPotionType.equals("Cold Resistance")) {
                if (rightGroup.stream().anyMatch(item -> item.getPotionType().equals("strength"))) {
                    rightGroupPotionType = "Strength";
                } else if (rightGroup.stream().anyMatch(item -> item.getPotionType().equals("cold"))) {
                    rightGroupPotionType = "Cold Resistance";
                }
            }
        }

        // Define Y positions for the rows with proper spacing
        int ingredientsY1 = 220;   // First ingredients row
        int ingredientsY2 = 390;   // Second ingredients row

        // Define X center positions for each row's ingredients
        int screenCenterX = GameConstants.WINDOW_WIDTH / 2;

        // Calculate starting X position for each group to be centered
        int totalWidth1 = (leftGroup.size() * INGREDIENT_SIZE) + ((leftGroup.size() - 1) * 10);
        int startX1 = screenCenterX - (totalWidth1 / 2);

        int totalWidth2 = (rightGroup.size() * INGREDIENT_SIZE) + ((rightGroup.size() - 1) * 10);
        int startX2 = screenCenterX - (totalWidth2 / 2);

        // Animation parameters
        final int ANIMATION_DELAY = 400; // ms between ingredients
        final int ANIMATION_DURATION = 800; // ms for each ingredient animation

        // --- INSERTION SORT ANIMATION ---
        // We'll do one group at a time

        // Create a list to track which ingredients have been animated
        final List<IngredientItem> animatedLeft = new ArrayList<>();
        final List<IngredientItem> animatedRight = new ArrayList<>();

        // Create and start a timer for the insertion sort animation
        final int[] leftIndex = {0};
        final int[] rightIndex = {0};
        final boolean[] isAnimatingLeft = {true}; // Start with left group

        Timer insertionTimer = new Timer(ANIMATION_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isAnimatingLeft[0]) {
                    // Animate left group
                    if (leftIndex[0] < leftGroup.size()) {
                        // Get current ingredient
                        IngredientItem ingredient = leftGroup.get(leftIndex[0]);

                        // Store the original position if not already set
                        if (ingredient.getOriginalPosition() == null) {
                            ingredient.setOriginalPosition(ingredient.getLocation());
                        }

                        // Calculate final position
                        int finalX = startX1 + (leftIndex[0] * (INGREDIENT_SIZE + 10));

                        // Add to animated list
                        animatedLeft.add(ingredient);

                        // Make sure it's visible and on top
                        ingredient.setSelected(true);
                        gridPanel.setComponentZOrder(ingredient, 0);

                        // Create animation
                        animateIngredientInsertionSort(
                            ingredient, 
                            finalX, 
                            ingredientsY1, 
                            ANIMATION_DURATION,
                            true,  // This is the currently inserted item
                            animatedLeft, 
                            () -> {
                                ingredient.setSelected(false);
                                leftIndex[0]++;
                            }
                        );

                    } else {
                        // Left group complete, switch to right group
                        isAnimatingLeft[0] = false;
                    }
                } else {
                    // Animate right group
                    if (rightIndex[0] < rightGroup.size()) {
                        // Get current ingredient
                        IngredientItem ingredient = rightGroup.get(rightIndex[0]);

                        // Store the original position if not already set
                        if (ingredient.getOriginalPosition() == null) {
                            ingredient.setOriginalPosition(ingredient.getLocation());
                        }

                        // Calculate final position
                        int finalX = startX2 + (rightIndex[0] * (INGREDIENT_SIZE + 10));

                        // Add to animated list
                        animatedRight.add(ingredient);

                        // Make sure it's visible and on top
                        ingredient.setSelected(true);
                        gridPanel.setComponentZOrder(ingredient, 0);

                        // Create animation
                        animateIngredientInsertionSort(
                            ingredient, 
                            finalX, 
                            ingredientsY2, 
                            ANIMATION_DURATION,
                            true,  // This is the currently inserted item
                            animatedRight, 
                            () -> {
                                ingredient.setSelected(false);
                                rightIndex[0]++;
                            }
                        );

                    } else {
                        // Animation complete
                        ((Timer)e.getSource()).stop();

                        // Enable check button
                        checkButton.setEnabled(true);

                        // Update instruction text
                        instructionLabel.setText("The ingredients have been sorted using insertion sort. Check your results.");

                        // Force repaint to ensure everything is visible
                        gridPanel.revalidate();
                        gridPanel.repaint();
                    }
                }
            }
        });

        // Start the animation
        insertionTimer.start();
    }



    
    
    
    // In TimSortVisualization.java, modify the animateIngredientInsertionSort method:
    private void animateIngredientInsertionSort(
        IngredientItem ingredient, 
        int targetX, 
        int targetY, 
        int duration,
        boolean isCurrentInsert,
        List<IngredientItem> animatedGroup,
        Runnable onComplete
    ) {
        Point start = ingredient.getLocation();

        // Use a FIXED number of frames and a faster timing
        final int FRAMES = 15; // fewer frames = faster animation
        final int FRAME_DURATION = 15; // milliseconds (faster refresh rate)

        final int[] currentFrame = {0};

        // Use a direct animation instead of three phases
        Timer animationTimer = new Timer(FRAME_DURATION, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentFrame[0]++;
                float progress = (float)currentFrame[0] / FRAMES;

                // Simple easing function
                float easedProgress = (float)(1 - Math.cos(progress * Math.PI)) / 2;

                // Direct path with slight arc
                int newX = (int)(start.x + (targetX - start.x) * easedProgress);

                // Add a slight arc for visual appeal
                int arcHeight = 30;
                float arcFactor = (float)(Math.sin(Math.PI * easedProgress) * arcHeight);
                int newY = (int)(start.y + (targetY - start.y) * easedProgress - arcFactor);

                ingredient.setLocation(newX, newY);

                // Force immediate repaint of just this component
                gridPanel.repaint(new Rectangle(newX-5, newY-5, 
                              ingredient.getWidth()+10, ingredient.getHeight()+10));

                if (currentFrame[0] >= FRAMES) {
                    ((Timer)e.getSource()).stop();

                    // Ensure final position is exact
                    ingredient.setLocation(targetX, targetY);

                    // Call completion callback
                    if (onComplete != null) {
                        SwingUtilities.invokeLater(onComplete);
                    }
                }
            }
        });

        // Start the animation
        animationTimer.start();
    }



    
    
    
    
    
    
    
    /**
    * Display potion options in Phase 3 based on potions identified in Phase 2
    */
    private void displayPotionOptions() {
        // Clear the grid panel
        gridPanel.removeAll();

        // Define the potion image size
        final int POTION_IMAGE_SIZE = 220;
        final int LABEL_HEIGHT = 25;
        final int DESC_HEIGHT = 60;
        final int PADDING_BELOW_IMAGE = 20;
        final int PADDING_BELOW_TITLE = 10;

        // Calculate vertical positions with proper spacing
        final int IMAGE_Y = 180;
        final int TITLE_Y = IMAGE_Y + POTION_IMAGE_SIZE + PADDING_BELOW_IMAGE;
        final int DESC_Y = TITLE_Y + LABEL_HEIGHT + PADDING_BELOW_TITLE;

        // DYNAMIC POTION DETERMINATION - Based on selected ingredients from Phase 1
        // Get potion types from Phase 2
        String leftPotionType = leftGroupPotionType;
        String rightPotionType = rightGroupPotionType;

        System.out.println("DEBUG: Displaying potion options - Left: " + leftPotionType + ", Right: " + rightPotionType);

        // Define potion information lookup
        Map<String, String[]> potionInfo = new HashMap<>();
        potionInfo.put("Fire Resistance", new String[]{"Fire Resistance Potion", 
                                         "Protects against fire attacks and extreme heat.",
                                         "/gameproject/resources/potions/fire_resistance_potion.png"});
        potionInfo.put("Cold Resistance", new String[]{"Cold Resistance Potion", 
                                         "Protects against ice attacks and freezing temperatures.",
                                         "/gameproject/resources/potions/cold_resistance_potion.png"});
        potionInfo.put("Strength", new String[]{"Strength Potion", 
                                    "Enhances physical strength and combat abilities.",
                                    "/gameproject/resources/potions/strength_potion.png"});
        potionInfo.put("Dexterity", new String[]{"Dexterity Potion", 
                                     "Improves agility, reflexes, and movement speed.",
                                     "/gameproject/resources/potions/dexterity_potion.png"});

        // Add boss-specific descriptions for Level 2
        if (gameLevel == 2) {
            // For Level 2 (Toxitar), add special descriptions
            if (leftPotionType.equals("Dexterity")) {
                potionInfo.put("Dexterity", new String[]{"Dexterity Potion", 
                    "Grants exceptional agility and reflexes - perfect for avoiding Toxitar's poison clouds.",
                    "/gameproject/resources/potions/dexterity_potion.png"});
            } else {
                // For other potion types, explain their effectiveness against Toxitar
                if (leftPotionType.equals("Fire Resistance")) {
                    potionInfo.put("Fire Resistance", new String[]{"Fire Resistance Potion", 
                        "Protects against fire, but offers little defense against Toxitar's poison.",
                        "/gameproject/resources/potions/fire_resistance_potion.png"});
                } else if (leftPotionType.equals("Strength")) {
                    potionInfo.put("Strength", new String[]{"Strength Potion", 
                        "Enhances physical power, but may not help avoid Toxitar's spreading poison.",
                        "/gameproject/resources/potions/strength_potion.png"});
                }
            }

            // Do the same for right potion type
            if (rightPotionType.equals("Dexterity")) {
                potionInfo.put("Dexterity", new String[]{"Dexterity Potion", 
                    "Grants exceptional agility and reflexes - perfect for avoiding Toxitar's poison clouds.",
                    "/gameproject/resources/potions/dexterity_potion.png"});
            } else {
                // For other potion types, explain their effectiveness against Toxitar
                if (rightPotionType.equals("Fire Resistance")) {
                    potionInfo.put("Fire Resistance", new String[]{"Fire Resistance Potion", 
                        "Protects against fire, but offers little defense against Toxitar's poison.",
                        "/gameproject/resources/potions/fire_resistance_potion.png"});
                } else if (rightPotionType.equals("Strength")) {
                    potionInfo.put("Strength", new String[]{"Strength Potion", 
                        "Enhances physical power, but may not help avoid Toxitar's spreading poison.",
                        "/gameproject/resources/potions/strength_potion.png"});
                }
            }
        } else {
            // For Level 1 (Flameclaw), add special descriptions
            if (leftPotionType.equals("Fire Resistance")) {
                potionInfo.put("Fire Resistance", new String[]{"Fire Resistance Potion", 
                    "Protects against fire attacks and extreme heat - ideal against Flameclaw.",
                    "/gameproject/resources/potions/fire_resistance_potion.png"});
            }

            if (rightPotionType.equals("Fire Resistance")) {
                potionInfo.put("Fire Resistance", new String[]{"Fire Resistance Potion", 
                    "Protects against fire attacks and extreme heat - ideal against Flameclaw.",
                    "/gameproject/resources/potions/fire_resistance_potion.png"});
            }
        }

        // Get position for left potion
        int leftX = GameConstants.WINDOW_WIDTH / 4 - (POTION_IMAGE_SIZE / 2);

        // Create invisible components for the left potion
        // 1. Get potion info
        String[] leftPotionData = potionInfo.get(leftPotionType);
        String leftPotionName = leftPotionData[0];
        String leftPotionDesc = leftPotionData[1];
        String leftPotionImgPath = leftPotionData[2];

        // 2. Create potion image label
        ImageIcon leftPotionImg = resourceManager.getImage(leftPotionImgPath);
        if (leftPotionImg != null) {
            JLabel leftImgLabel = new JLabel(new ImageIcon(leftPotionImg.getImage().getScaledInstance(
                    POTION_IMAGE_SIZE, POTION_IMAGE_SIZE, Image.SCALE_SMOOTH)));
            leftImgLabel.setBounds(leftX, IMAGE_Y, POTION_IMAGE_SIZE, POTION_IMAGE_SIZE);
            leftImgLabel.setVisible(false); // Initially invisible
            gridPanel.add(leftImgLabel);
        }

        // 3. Create potion title label - CENTERED OVER POTION
        JLabel leftTitleLabel = new JLabel(leftPotionName, JLabel.CENTER);
        leftTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        // For Level 2, make the Dexterity Potion title stand out if it's this one
        if (gameLevel == 2 && leftPotionType.equals("Dexterity")) {
            leftTitleLabel.setForeground(new Color(50, 255, 50)); // Bright green for Dexterity in Level 2
        } else {
            leftTitleLabel.setForeground(Color.WHITE);
        }

        // Center the title over the potion
        int leftTitleWidth = POTION_IMAGE_SIZE + 130;
        leftTitleLabel.setBounds(leftX - (leftTitleWidth - POTION_IMAGE_SIZE)/2, TITLE_Y, leftTitleWidth, LABEL_HEIGHT);
        leftTitleLabel.setVisible(false); // Initially invisible
        gridPanel.add(leftTitleLabel);

        // 4. Create potion description
        JLabel leftDescLabel = new JLabel("<html><div style='text-align:center;'>" + leftPotionDesc + "</div></html>", JLabel.CENTER);
        leftDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        leftDescLabel.setForeground(Color.WHITE);
        // Center the description below the title with same width
        leftDescLabel.setBounds(leftX - (leftTitleWidth - POTION_IMAGE_SIZE)/2, DESC_Y, leftTitleWidth, DESC_HEIGHT);
        leftDescLabel.setVisible(false); // Initially invisible
        gridPanel.add(leftDescLabel);

        // Get position for right potion
        int rightX = (GameConstants.WINDOW_WIDTH * 3) / 4 - (POTION_IMAGE_SIZE / 2);

        // Create invisible components for the right potion
        // 1. Get potion info
        String[] rightPotionData = potionInfo.get(rightPotionType);
        String rightPotionName = rightPotionData[0];
        String rightPotionDesc = rightPotionData[1];
        String rightPotionImgPath = rightPotionData[2];

        // 2. Create potion image label
        ImageIcon rightPotionImg = resourceManager.getImage(rightPotionImgPath);
        if (rightPotionImg != null) {
            JLabel rightImgLabel = new JLabel(new ImageIcon(rightPotionImg.getImage().getScaledInstance(
                    POTION_IMAGE_SIZE, POTION_IMAGE_SIZE, Image.SCALE_SMOOTH)));
            rightImgLabel.setBounds(rightX, IMAGE_Y, POTION_IMAGE_SIZE, POTION_IMAGE_SIZE);
            rightImgLabel.setVisible(false); // Initially invisible
            gridPanel.add(rightImgLabel);
        }

        // 3. Create potion title label - CENTERED OVER POTION
        JLabel rightTitleLabel = new JLabel(rightPotionName, JLabel.CENTER);
        rightTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        // For Level 2, make the Dexterity Potion title stand out if it's this one
        if (gameLevel == 2 && rightPotionType.equals("Dexterity")) {
            rightTitleLabel.setForeground(new Color(50, 255, 50)); // Bright green for Dexterity in Level 2
        } else {
            rightTitleLabel.setForeground(Color.WHITE);
        }

        // Center the title over the potion
        int rightTitleWidth = POTION_IMAGE_SIZE + 130;
        rightTitleLabel.setBounds(rightX - (rightTitleWidth - POTION_IMAGE_SIZE)/2, TITLE_Y, rightTitleWidth, LABEL_HEIGHT);
        rightTitleLabel.setVisible(false); // Initially invisible
        gridPanel.add(rightTitleLabel);

        // 4. Create potion description
        JLabel rightDescLabel = new JLabel("<html><div style='text-align:center;'>" + rightPotionDesc + "</div></html>", JLabel.CENTER);
        rightDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rightDescLabel.setForeground(Color.WHITE);
        // Center the description below the title with same width
        rightDescLabel.setBounds(rightX - (rightTitleWidth - POTION_IMAGE_SIZE)/2, DESC_Y, rightTitleWidth, DESC_HEIGHT);
        rightDescLabel.setVisible(false); // Initially invisible
        gridPanel.add(rightDescLabel);

        // Create potion option labels for click detection (behind the scenes)
        IngredientItem leftPotionItem = new IngredientItem(1, "blue");
        leftPotionItem.setPotionType(leftPotionName);
        leftPotionItem.setGroupLabel(true);
        leftPotionItem.setLocation(leftX, IMAGE_Y);
        leftPotionItem.setSize(POTION_IMAGE_SIZE, POTION_IMAGE_SIZE);
        leftPotionItem.setVisible(false); // Initially invisible
        gridPanel.add(leftPotionItem);

        IngredientItem rightPotionItem = new IngredientItem(2, "red");
        rightPotionItem.setPotionType(rightPotionName);
        rightPotionItem.setGroupLabel(true);
        rightPotionItem.setLocation(rightX, IMAGE_Y);
        rightPotionItem.setSize(POTION_IMAGE_SIZE, POTION_IMAGE_SIZE);
        rightPotionItem.setVisible(false); // Initially invisible
        gridPanel.add(rightPotionItem);

        // Update instruction based on game level
        if (gameLevel == 2) {
            instructionLabel.setText("Use your 'Mind of Unity' to choose which potion will be most effective against Toxitar's poison clouds.");
        } else {
            instructionLabel.setText("Use your 'Mind of Unity' to choose which potion to craft. Click the button below.");
        }

        // CRITICAL FIX: Explicitly enable ability button
        abilityButton.setText("Use Mind of Unity");
        setAbilityButtonEnabled(true);
        System.out.println("DEBUG: Ability button explicitly enabled in displayPotionOptions");

        // Disable check button until a potion is selected
        checkButton.setEnabled(false);

        // Add click listeners to potions for selection
        leftPotionItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (leftPotionItem.isVisible()) {
                    // Select the LEFT potion
                    craftedPotion = leftPotionName;

                    // Show confirmation with correct potion name
                    JOptionPane.showMessageDialog(TimSortVisualization.this,
                        "You've selected the " + leftPotionName + "!",
                        "Potion Selection",
                        JOptionPane.INFORMATION_MESSAGE
                    );

                    // Enable check button
                    checkButton.setEnabled(true);
                    phaseCompleted = true;
                }
            }
        });

        rightPotionItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (rightPotionItem.isVisible()) {
                    // Select the RIGHT potion
                    craftedPotion = rightPotionName;

                    // Show confirmation with correct potion name
                    JOptionPane.showMessageDialog(TimSortVisualization.this,
                        "You've selected the " + rightPotionName + "!",
                        "Potion Selection",
                        JOptionPane.INFORMATION_MESSAGE
                    );

                    // Enable check button
                    checkButton.setEnabled(true);
                    phaseCompleted = true;
                }
            }
        });

        // Force UI update
        gridPanel.revalidate();
        gridPanel.repaint();
    }    

    
    /**
    * Modified check phase completion to account for automatic sorting
    */
    private void checkPhaseCompletion() {
        if (currentPhase == 1) {
            // Phase 1 logic remains unchanged
            if (selectedIngredients.size() == MAX_SELECTIONS) {
                boolean hasValidRuns = checkValidRuns();

                if (hasValidRuns) {
                    // Show phase completion dialogue first with appropriate level context
                    if (gameLevel == 3) {
                        showPhaseDialogue("level3_phase1_end");
                    } else if (gameLevel == 2) {
                        showPhaseDialogue("level2_phase1_end");
                    } else {
                        showPhaseDialogue("phase1_end");
                    }

                    // Phase transition will happen after dialogue ends
                    Timer transitionTimer = new Timer(500, e -> {
                        advanceToNextPhase();
                    });
                    transitionTimer.setRepeats(false);
                    transitionTimer.start();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "These ingredients don't form proper sequences. Try again!",
                        "Try Again",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        } else if (currentPhase == 2) {
            // Phase 2 logic remains unchanged
            boolean leftSorted = isGroupSorted(leftGroup);
            boolean rightSorted = isGroupSorted(rightGroup);

            if (isLeftGroupSorted && isRightGroupSorted && leftSorted && rightSorted) {
                // Set phase as completed
                phaseCompleted = true;

                // Get the NarrativeSystem instance
                NarrativeSystem narrativeSystem = NarrativeSystem.getInstance();

                // Create dialogue overlay
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

                // Add overlay at the highest z-order
                add(dialogueOverlay, 0);
                setComponentZOrder(dialogueOverlay, 0);

                // Get dynamic dialogue using the actual potion types detected
                List<NarrativeSystem.DialogueEntry> phase2EndDialogue;

                // Select the appropriate dialogue based on game level
                if (gameLevel == 3) {
                    phase2EndDialogue = narrativeSystem.getDynamicPhase2EndDialogue(leftGroupPotionType, rightGroupPotionType);
                    // Update to include Lord Chaosa specific context
                    for (NarrativeSystem.DialogueEntry entry : phase2EndDialogue) {
                        if (entry.getText().contains("Choose wisely")) {
                            String newText = entry.getText().replace("Choose wisely", "Choose wisely against Lord Chaosa's reality warping");
                            // We can't modify the entry directly, but we're ensuring the context is appropriate
                        }
                    }
                } else if (gameLevel == 2) {
                    phase2EndDialogue = narrativeSystem.getDynamicPhase2EndDialogue(leftGroupPotionType, rightGroupPotionType);
                    // Update to include Toxitar specific context
                    for (NarrativeSystem.DialogueEntry entry : phase2EndDialogue) {
                        if (entry.getText().contains("Choose wisely")) {
                            String newText = entry.getText().replace("Choose wisely", "Choose wisely against Toxitar's poison");
                            // We can't modify the entry directly, but we're ensuring the context is appropriate
                        }
                    }
                } else {
                    phase2EndDialogue = narrativeSystem.getDynamicPhase2EndDialogue(leftGroupPotionType, rightGroupPotionType);
                }

                // Create a dialogue manager specifically for this overlay
                DialogueManager tempDialogueManager = new DialogueManager(controller);
                tempDialogueManager.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
                dialogueOverlay.add(tempDialogueManager);

                // Start the dialogue sequence
                tempDialogueManager.startDialogue(phase2EndDialogue);

                // Pause any active timers or animations during dialogue
                pauseDuringDialogue();

                // Add a listener to remove the overlay when dialogue ends
                tempDialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
                    @Override
                    public void onDialogueEnd() {
                        // Remove the overlay when dialogue completes
                        remove(dialogueOverlay);
                        repaint();

                        // Resume game elements 
                        resumeAfterDialogue();

                        // Advance to Phase 3 after a short delay
                        Timer transitionTimer = new Timer(500, e -> {
                            advanceToNextPhase();
                        });
                        transitionTimer.setRepeats(false);
                        transitionTimer.start();
                    }
                });

                // Force revalidate and repaint
                revalidate();
                repaint();
            } else if (!isLeftGroupSorted || !isRightGroupSorted) {
                // They haven't used the ability yet
                JOptionPane.showMessageDialog(this,
                    "Use the 'Hand of Balance' ability first to sort the ingredients!",
                    "Try Again",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                // Something is wrong with the sorting
                JOptionPane.showMessageDialog(this,
                    "The ingredients aren't properly sorted yet. Try again!",
                    "Try Again",
                    JOptionPane.WARNING_MESSAGE
                );
                isLeftGroupSorted = false;
                isRightGroupSorted = false;
            }
        } else if (currentPhase == 3) {
            // Phase 3 - Use dynamic boss name
            if (craftedPotion != null) {
                // Get the appropriate dialogue key based on game level
                String dialogueKey;
                if (gameLevel == 3) {
                    dialogueKey = "level3_phase3_end";
                } else if (gameLevel == 2) {
                    dialogueKey = "level2_phase3_end";
                } else {
                    dialogueKey = "phase3_end";
                }

                // Show the phase 3 end dialogue
                showPhaseDialogue(dialogueKey);
            }
        }
    }


    
    /**
    * Modified startBossBattle method with boss name parameter to handle different bosses
    */
    private void startBossBattle(String bossName) {
        // Add debug logging
        System.out.println("DEBUG: Starting boss battle against " + bossName);

        
        gridPanel.removeAll();
        gridPanel.setVisible(false);
    
    
        // Create semi-transparent overlay with boss-specific background color
        JPanel battleOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Boss-specific background colors
                if (bossName.equals("LordChaosa")) {
                    // Dark purple background for Lord Chaosa
                    g.setColor(new Color(40, 0, 60, 200));
                } else if (bossName.equals("Toxitar")) {
                    // Dark green background for Toxitar
                    g.setColor(new Color(0, 40, 20, 200));
                } else {
                    // Dark red background for Flameclaw
                    g.setColor(new Color(40, 0, 0, 200));
                }
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        battleOverlay.setLayout(null);
        battleOverlay.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        battleOverlay.setOpaque(false);
        add(battleOverlay, 0);

        // Ensure battle overlay is on top
        setComponentZOrder(battleOverlay, 0);

        // Load boss image - IMPORTANT: add debug statements
        String bossImagePath;
            if (bossName.equals("LordChaosa")) {
                bossImagePath = "/gameproject/resources/characters/lord_chaosa.png";
            } else if (bossName.equals("Toxitar")) {
                bossImagePath = "/gameproject/resources/characters/toxitar.png";
            } else {
                bossImagePath = "/gameproject/resources/characters/flameclaw.png";
            }
        System.out.println("DEBUG: Looking for boss image at path: " + bossImagePath);

        ImageIcon bossImage = resourceManager.getImage(bossImagePath);
        System.out.println("DEBUG: Boss image loaded: " + (bossImage != null));
        
        
        

        if (bossImage != null) {
            // Scale boss image
            Image scaledBossImage = bossImage.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            JLabel bossLabel = new JLabel(new ImageIcon(scaledBossImage));
            bossLabel.setBounds((GameConstants.WINDOW_WIDTH - 300) / 2, 100, 300, 300);
            battleOverlay.add(bossLabel);

            // Debug: Verify boss label was added
            System.out.println("DEBUG: Boss label added to battle overlay");

            // Add boss name with appropriate color
            JLabel bossNameLabel = new JLabel(bossName, JLabel.CENTER);
            bossNameLabel.setFont(new Font("SansSerif", Font.BOLD, 36));

            // Set boss-specific color
            if (bossName.equals("LordChaosa")) {
                bossNameLabel.setForeground(new Color(200, 50, 255)); // Purple for Lord Chaosa
            } else if (bossName.equals("Toxitar")) {
                bossNameLabel.setForeground(new Color(50, 200, 50)); // Green for Toxitar
            } else {
                bossNameLabel.setForeground(new Color(255, 50, 50)); // Red for Flameclaw
            }

            bossNameLabel.setBounds(0, 420, GameConstants.WINDOW_WIDTH, 40);
            battleOverlay.add(bossNameLabel);

            // Add battle text - with correct boss-specific battle cry
            String battleCry = "Battle in progress...";
            if (bossName.equals("Flameclaw")) {
                battleCry = "BURN! ALL WILL BURN!";
            } else if (bossName.equals("Toxitar")) {
                battleCry = "POISON... FILLS... THE AIR!";
            } else if (bossName.equals("LordChaosa")) {
                battleCry = "REALITY IS MINE TO COMMAND!";
            }
            
            

            JLabel battleText = new JLabel(battleCry, JLabel.CENTER);
            battleText.setFont(new Font("SansSerif", Font.BOLD, 24));
            battleText.setForeground(Color.WHITE);
            battleText.setBounds(0, 470, GameConstants.WINDOW_WIDTH, 30);
            battleOverlay.add(battleText);

            // Debug: Check component count to ensure everything is added
            System.out.println("DEBUG: Total components in battle overlay: " + battleOverlay.getComponentCount());

            // Add animated battle effects based on boss type
            startBattleEffects(battleOverlay, bossName);

            // Force repaint to ensure all components are visible
            battleOverlay.revalidate();
            battleOverlay.repaint();
        } else {
            System.err.println("ERROR: Failed to load boss image for " + bossName);
            // Add a fallback image or text if boss image couldn't be loaded
            JLabel errorLabel = new JLabel("Facing " + bossName + "...", JLabel.CENTER);
            errorLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
            errorLabel.setForeground(Color.WHITE);
            errorLabel.setBounds(0, 100, GameConstants.WINDOW_WIDTH, 300);
            battleOverlay.add(errorLabel);
        }

        // After a delay, show battle outcome
        Timer battleTimer = new Timer(5000, e -> {
            // IMPORTANT: Remove battle overlay before proceeding
            remove(battleOverlay);

            // Extract the actual potion type name from the craftedPotion string
            String selectedPotionType = craftedPotion;
            if (selectedPotionType.endsWith(" Potion")) {
                selectedPotionType = selectedPotionType.substring(0, selectedPotionType.length() - 7);
            }
            System.out.println("DEBUG: Extracted potion type: " + selectedPotionType);

            // Determine the correct potion to use against this boss
            boolean correctChoice = false;

            // Check against the current boss
            if (bossName.equals("Flameclaw")) {
                correctChoice = selectedPotionType.equals("Fire Resistance");
            } else if (bossName.equals("Toxitar")) {
                correctChoice = selectedPotionType.equals("Dexterity");
            } else if (bossName.equals("LordChaosa")) {
                correctChoice = selectedPotionType.equals("Strength");
            }

            // IMPORTANT: Clear the grid panel completely before showing result
            gridPanel.removeAll();
            gridPanel.setVisible(false);

            // IMPORTANT: Properly finish the phase before showing result
            finishCurrentPhase();

            // Store the selected potion in the model for use in dialogue
            controller.model.setSelectedPotion(craftedPotion);

            // Determine boss level based on name
            int bossLevel = 1;
            if (bossName.equals("Toxitar")) {
                bossLevel = 2;
            } else if (bossName.equals("LordChaosa")) {
                bossLevel = 3;
            }

            // IMPORTANT: Add debug to see what's happening
            System.out.println("DEBUG: Signaling boss battle complete to controller with boss level: " + bossLevel);

            // Show a brief message about the battle outcome
            if (correctChoice) {
                JOptionPane.showMessageDialog(this,
                    "Excellent choice! The " + selectedPotionType + " Potion was effective against " + bossName + "!",
                    "Success!",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(this,
                    "Oh no! The " + selectedPotionType + " Potion wasn't effective against " + bossName + "!",
                    "Failure",
                    JOptionPane.WARNING_MESSAGE
                );
            }

            // Signal successful boss battle
            controller.onBossBattleComplete(correctChoice, bossLevel);
        });
        battleTimer.setRepeats(false);
        battleTimer.start();
    }

    
    
    
    
    
    
    
    
    /**
    * New helper method to properly finish the current phase
    * This ensures a clean transition after battle
    */
    private void finishCurrentPhase() {
        // Clear all UI elements that might be causing issues
        gridPanel.removeAll();
        gridPanel.setVisible(false);

        // Reset any phase-specific state to prevent it from reappearing
        allIngredients.clear();
        selectedIngredients.clear();
        identifiedRuns.clear();
        leftGroup.clear();
        rightGroup.clear();
        mergedItems.clear();
        craftedPotion = null;

        // Reset phase UI elements completely
        abilityButton.setEnabled(false);
        checkButton.setEnabled(false);

        // Mark phase as fully completed
        phaseCompleted = true;

        // Force a complete UI refresh
        revalidate();
        repaint();
    }

    
    
    
    
    

    private void startBattleEffects(JPanel overlay, String bossName) {
        // Debug the boss name to ensure correct effects are applied
        System.out.println("DEBUG: Starting battle effects for boss: " + bossName);

        // Create animated battle effects
        Random rand = new Random();

        // Create effects appropriate for the current boss
        Timer effectsTimer = new Timer(100, new ActionListener() {
            int count = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (count > 50) {
                    ((Timer)e.getSource()).stop();
                    return;
                }

                // Create a new effect
                JPanel effect = new JPanel() {
                    // Different effects for different bosses
                    Color color;
                    int size;

                    {
                        if (bossName.equals("Toxitar")) {
                            // Toxitar - green poison effects
                            color = new Color(
                                rand.nextInt(100),
                                rand.nextInt(100) + 155,
                                rand.nextInt(50),
                                rand.nextInt(100) + 155);
                            size = rand.nextInt(25) + 15; // Larger poison clouds
                        } else if (bossName.equals("LordChaosa")) {
                            // Lord Chaosa - ENHANCED purple/magenta reality distortion effects
                            color = new Color(
                                rand.nextInt(100) + 155,
                                rand.nextInt(50),
                                rand.nextInt(100) + 155,
                                rand.nextInt(100) + 155);
                            size = rand.nextInt(50) + 30; // Much larger reality distortion effects

                            // Debug when Lord Chaosa effect is created
                            System.out.println("DEBUG: Created LordChaosa effect, size: " + size);
                        } else {
                            // Flameclaw - fire effects
                            color = new Color(
                                rand.nextInt(100) + 155,
                                rand.nextInt(100),
                                rand.nextInt(50),
                                rand.nextInt(100) + 155);
                            size = rand.nextInt(20) + 10; // Smaller fire sparks
                        }
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);

                        // Fill with the effect color
                        g.setColor(color);
                        g.fillOval(0, 0, getWidth(), getHeight());

                        // For Lord Chaosa, add extra "reality distortion" effect
                        if (bossName.equals("LordChaosa")) {
                            Graphics2D g2d = (Graphics2D) g;
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

                            // Draw distortion rings
                            g2d.setColor(new Color(255, 50, 255, 180));
                            g2d.drawOval(5, 5, getWidth() - 10, getHeight() - 10);
                            g2d.drawOval(10, 10, getWidth() - 20, getHeight() - 20);

                            // Reset composite
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                        }
                    }
                };

                // Set random position around boss based on boss type
                int x, y;
                if (bossName.equals("Toxitar")) {
                    // Toxitar's poison spreads more on the ground
                    x = (GameConstants.WINDOW_WIDTH / 2) + rand.nextInt(400) - 200;
                    y = 300 + rand.nextInt(150);
                } else if (bossName.equals("LordChaosa")) {
                    // Lord Chaosa's reality distortions appear all around
                    x = (GameConstants.WINDOW_WIDTH / 2) + rand.nextInt(600) - 300;
                    y = 200 + rand.nextInt(400) - 100;

                    // Debug Lord Chaosa effect positioning
                    System.out.println("DEBUG: LordChaosa effect positioned at: " + x + "," + y);
                } else {
                    // Flameclaw's fire comes more from the center
                    x = (GameConstants.WINDOW_WIDTH / 2) + rand.nextInt(300) - 150;
                    y = 250 + rand.nextInt(200) - 100;
                }

                // Instead of trying to access effect.size, calculate the size here directly
                int effectSize;
                if (bossName.equals("Toxitar")) {
                    effectSize = rand.nextInt(25) + 15;
                } else if (bossName.equals("LordChaosa")) {
                    effectSize = rand.nextInt(50) + 30;
                } else {
                    effectSize = rand.nextInt(20) + 10;
                }

                // Now use effectSize
                effect.setBounds(x, y, effectSize, effectSize);
                effect.setOpaque(false);

                overlay.add(effect);

                // Ensure effect is visible by setting it to the top of the z-order
                overlay.setComponentZOrder(effect, 0);

                overlay.revalidate();
                overlay.repaint();

                // Remove effect after a delay
                Timer remover = new Timer(rand.nextInt(1000) + 500, event -> {
                    overlay.remove(effect);
                    overlay.repaint();
                });
                remover.setRepeats(false);
                remover.start();

                count++;
            }
        });

        effectsTimer.start();
    }

    
    
    
    /**
     * Check if selected ingredients form valid runs
     */
    private boolean checkValidRuns() {
        // For simplicity, we'll count how many selected ingredients belong to the identified runs
        int validRunIngredients = 0;
        
        for (IngredientItem ingredient : selectedIngredients) {
            for (List<IngredientItem> run : identifiedRuns) {
                if (run.contains(ingredient)) {
                    validRunIngredients++;
                    break;
                }
            }
        }
        
        // At least 80% of selections should be from valid runs
        return validRunIngredients >= (MAX_SELECTIONS * 0.8);
    }
    
    /**
     * Check if a group is properly sorted
     */
    private boolean isGroupSorted(List<IngredientItem> group) {
        if (group.size() != GROUP_SIZE) {
            return false;
        }

        for (int i = 0; i < group.size() - 1; i++) {
            if (group.get(i).getValue() > group.get(i + 1).getValue()) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * Advance to the next phase
     */
    private void advanceToNextPhase() {
        // Stop all active timers first
        for (Timer timer : activeTimers) {
            if (timer.isRunning()) {
                timer.stop();
            }
        }
        activeTimers.clear();

        currentPhase++;
        phaseCompleted = false;

        // Reset everything - crucial for clean transition
        gridPanel.removeAll();
        
        System.out.println("DEBUG: Advancing to phase " + currentPhase);
        loadBackground(currentPhase);
        

        // Update phase label
        if (currentPhase == 2) {
            phaseLabel.setText("Phase 2: The Hand of Balance");

            // Load Hand of Balance ability icons
            loadPhaseAbilityIcons(2);

            // Update button text
            updateAbilityButtonText("Use Hand of Balance");

            // Update the controller about the phase change for dynamic dialogue
            controller.onPhaseAdvance(currentPhase, leftGroupPotionType, rightGroupPotionType);

            // Immediately arrange ingredients for Phase 2
            arrangeIngredientsForSorting();
        } else if (currentPhase == 3) {
            phaseLabel.setText("Phase 3: The Mind of Unity");

            // Load Mind of Unity ability icons
            loadPhaseAbilityIcons(3);

            // Update button text
            updateAbilityButtonText("Use Mind of Unity");

            // Update the controller about the phase change for dynamic dialogue
            controller.onPhaseAdvance(currentPhase, leftGroupPotionType, rightGroupPotionType);

            displayPotionOptions();
        } else {
            // Return to story mode after all phases
            controller.returnToStoryMode();
        }

        // Force complete UI refresh
        revalidate();
        repaint();
    }
    
    
    
    /**
    * Improved method to handle phase transitions with dynamic dialogue in TimSortVisualization
    */
    public void onPhaseAdvance(int phase, String leftPotionType, String rightPotionType) {
        System.out.println("DEBUG: Phase advanced to " + phase);
        System.out.println("DEBUG: Left potion type: " + leftPotionType);
        System.out.println("DEBUG: Right potion type: " + rightPotionType);

        // Store potion types for later use
        this.leftGroupPotionType = leftPotionType;
        this.rightGroupPotionType = rightPotionType;

        // Store potion types in model via controller for dialogue access
        controller.storePotionTypes(leftPotionType, rightPotionType);

        // Update UI to reflect the current phase
        updatePhaseLabel();
    }
    
    /**
     * Show a hint for the current phase
     */
    private void showHint() {
        String hint = getLevelSpecificHint(currentPhase);

        JOptionPane.showMessageDialog(this,
            hint,
            "Hint",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    


    
    /**
     * Create a styled button
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(60, 60, 120));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setPreferredSize(new Dimension(150, 40));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(80, 80, 160));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(60, 60, 120));
            }
        });
        
        return button;
    }
    
    // Make sure the paintComponent method is properly drawing the background
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the background image to fill the entire panel
        if (backgroundImage != null) {
            g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback to solid color if image is not available
            System.out.println("DEBUG: No background available, using solid color");
            g.setColor(new Color(25, 25, 50));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // Any additional decoration or UI elements would go here
    }
    
    /**
     * Custom button with animation states
     */
    private class AnimatedButton extends JButton {
        private ImageIcon normalIcon;
        private ImageIcon hoverIcon;
        private ImageIcon clickedIcon;
        private boolean isHovered = false;
        private boolean isClicked = false;

        public AnimatedButton(String text, ImageIcon normalIcon, ImageIcon hoverIcon, ImageIcon clickedIcon) {
            super(text);

            // Store the original icons
            this.normalIcon = normalIcon;
            this.hoverIcon = hoverIcon;
            this.clickedIcon = clickedIcon;

            // Configure the button appearance
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setHorizontalTextPosition(JButton.CENTER);
            setVerticalTextPosition(JButton.CENTER);

            // Force the UI to respect our font
            setUI(new BasicButtonUI());

            // Set the initial icon
            updateIcon();

            // Add mouse listeners for hover and click effects
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    updateIcon();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isClicked = false;
                    updateIcon();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isClicked = true;
                    updateIcon();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isClicked = false;
                    updateIcon();
                }
            });
        }

        @Override
        public void setSize(Dimension d) {
            super.setSize(d);
            updateIcon();
        }

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);
            updateIcon();
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);
            updateIcon();
        }

        /**
         * Resize an ImageIcon to fit the button dimensions
         */
        private ImageIcon resizeIcon(ImageIcon icon) {
            if (icon == null) return null;
            if (getWidth() <= 0 || getHeight() <= 0) return icon;

            Image img = icon.getImage();
            Image resizedImg = img.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImg);
        }

        /**
         * Update the button icon based on current state
         */
        private void updateIcon() {
            if (isClicked && clickedIcon != null) {
                setIcon(resizeIcon(clickedIcon));
            } else if (isHovered && hoverIcon != null) {
                setIcon(resizeIcon(hoverIcon));
            } else if (normalIcon != null) {
                setIcon(resizeIcon(normalIcon));
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            // Ensure icon is properly sized before painting
            updateIcon();
            super.paintComponent(g);
        }
    }
    
    
    
    @Override
        public void addNotify() {
            super.addNotify();

            // We can't directly access model, so just check the currentPhase value
            // If it's not set properly, default to phase 1
            if (currentPhase < 1 || currentPhase > 3) {
                currentPhase = 1; // Default to phase 1 if not set
            }

            // Update phase label
            updatePhaseLabel();

            // Initialize the proper phase UI
            initializePhaseUI();
        }
       
    
        
    /**
    * Configure visualization for a specific phase
    * This should be called by the controller when switching phases
    */
    public void setPhase(int phase) {
        if (phase >= 1 && phase <= 3) {
            System.out.println("DEBUG: Setting phase to " + phase);

            // First reset everything
            gridPanel.removeAll();

            // Set the new phase
            currentPhase = phase;

            // Load the appropriate background for this phase
            loadBackground(phase);

            // Load the appropriate ability icons for this phase
            loadPhaseAbilityIcons(phase);

            // Update phase label
            updatePhaseLabel();

            // ADDED: Ensure grid panel is correctly sized for all phases
            if (currentPhase == 1) {
                // Reset grid panel dimensions for Phase 1
                int cellSize = INGREDIENT_SIZE;
                int gridWidth = GRID_COLS * cellSize;
                int gridHeight = GRID_ROWS * cellSize;
                int GRID_PADDING = 12;
                int totalWidth = gridWidth + (GRID_PADDING * 2);
                int totalHeight = gridHeight + (GRID_PADDING * 2);
                int gridX = (GameConstants.WINDOW_WIDTH - totalWidth) / 2;
                int gridY = 135;
                gridPanel.setBounds(gridX, gridY, totalWidth, totalHeight);
            }

            // Update ability button text based on phase
            String buttonText = "Use " + getAbilityNameForPhase(phase);
            updateAbilityButtonText(buttonText);

            // CRITICAL FIX: Explicitly enable the ability button
            setAbilityButtonEnabled(true);

            // Then initialize the proper phase UI
            initializePhaseUI();

            // Add a slight delay before showing the start dialogue
            Timer dialogueTimer = new Timer(1000, e -> {
                // Show the appropriate dialogue for this phase
                switch (currentPhase) {
                    case 1:
                        showPhaseDialogue("phase1_start");
                        break;
                    case 2:
                        showPhaseDialogue("phase2_start");
                        break;
                    case 3:
                        showPhaseDialogue("phase3_start");
                        break;
                }
            });
            dialogueTimer.setRepeats(false);
            dialogueTimer.start();

            // Add this timer to the active timers list
            activeTimers.add(dialogueTimer);
        }
    }

    
    
    
    
    
    
   /**
    * Modified updatePhaseLabel method to show the correct boss context
    */
    private void updatePhaseLabel() {
        String bossContext = "";

        if (gameLevel == 3) {
            bossContext = " (Lord Chaosa)";
        } else if (gameLevel == 2) {
            bossContext = " (Toxitar)";
        } else {
            bossContext = " (Flameclaw)";
        }

        switch (currentPhase) {
            case 1:
                phaseLabel.setText("Phase 1: The Eye of Pattern" + bossContext);
                updateAbilityButtonText("Use Eye of Pattern");
                abilityNameLabel.setText("Eye of Pattern");

                // Update instruction based on boss
                if (gameLevel == 3) {
                    instructionLabel.setText("Identify ingredients that enhance strength to counter Lord Chaosa's reality distortions.");
                } else if (gameLevel == 2) {
                    instructionLabel.setText("Identify ingredients that enhance agility to evade Toxitar's poison.");
                } else {
                    instructionLabel.setText("Use your 'Eye of Pattern' ability to identify ingredient sequences (runs).");
                }
                break;

            case 2:
                phaseLabel.setText("Phase 2: The Hand of Balance" + bossContext);
                updateAbilityButtonText("Use Hand of Balance");

                // Update instruction based on boss
                if (gameLevel == 3) {
                    instructionLabel.setText("Sort the ingredients properly to craft a potion that enhances raw strength.");
                } else if (gameLevel == 2) {
                    instructionLabel.setText("Sort the ingredients properly to craft a potion that enhances agility.");
                } else {
                    instructionLabel.setText("Use your 'Hand of Balance' to sort ingredients into two groups.");
                }
                break;

            case 3:
                phaseLabel.setText("Phase 3: The Mind of Unity" + bossContext);
                updateAbilityButtonText("Use Mind of Unity");

                // Update instruction based on boss
                if (gameLevel == 3) {
                    instructionLabel.setText("Choose which potion will be most effective against Lord Chaosa's reality distortions.");
                } else if (gameLevel == 2) {
                    instructionLabel.setText("Choose which potion will be most effective against Toxitar's poison clouds.");
                } else {
                    instructionLabel.setText("Use your 'Mind of Unity' to choose which potion to craft.");
                }
                break;
        }
    }
    
    
    
    

    /**
    * Initialize UI for the current phase
    */
    private void initializePhaseUI() {
        // Reset UI elements
        gridPanel.removeAll();

        // Set proper background transparency based on phase
        if (currentPhase == 1) {
            // Only Phase 1 should have the grid background
            gridPanel.setBackground(null);
        } else {
            // Phase 2 and 3 should have completely transparent background
            gridPanel.setBackground(new Color(0, 0, 0, 0));
        }

        // Force repaint before setting up the new phase
        gridPanel.revalidate();
        gridPanel.repaint();

        // Set button labels with dynamic boss references
        updatePhaseLabel();

        // Perform phase-specific initialization
        switch (currentPhase) {
            case 1:
                // Generate ingredients for Phase 1
                generateIngredients();
                break;

            case 2:
                // Setup Phase 2 with scattered ingredients
                arrangeIngredientsForSorting();
                break;

            case 3:
                // Set up Phase 3 - but don't display potions yet
                displayPotionOptions();
                break;
        }

        // Reset state
        checkButton.setEnabled(false);
        phaseCompleted = false;

        // CRITICAL FIX: Make sure ability button is enabled
        setAbilityButtonEnabled(true);
        System.out.println("DEBUG: Ability button enabled in initializePhaseUI for phase " + currentPhase);

        // Force complete UI refresh
        revalidate();
        repaint();
    }

    
    
    
    /**
    * Ingredient item class representing a visual ingredient element that can be
    * selected, highlighted, and arranged with completely transparent background
    */
    private class IngredientItem extends JPanel {
        private int value;
        private String color;
        private String ingredientName;
        private String potionType;
        private boolean isSelected = false;
        private boolean isHighlighted = false;
        private boolean isBoxVisible = false;
        private Color highlightColor = new Color(255, 255, 0, 80);
        private boolean isGroupLabel = false;
        private Point originalPosition = null; // Added to store original position for animation

        private ImageIcon ingredientImage = null;
        private ImageIcon gridBoxImage = null;

        public IngredientItem(int value, String color) {
            this.value = value;
            this.color = color;

            // Critical for transparency - set panel to be completely transparent
            setOpaque(false);
            setBackground(new Color(0, 0, 0, 0));
            setSize(INGREDIENT_SIZE, INGREDIENT_SIZE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Load grid box image
            gridBoxImage = resourceManager.getImage("/gameproject/resources/grid_box.png");

            // Set grid boxes initially visible only in Phase 1
            this.isBoxVisible = (currentPhase == 1);
        }

        public Point getOriginalPosition() {
            return originalPosition;
        }

        public void setOriginalPosition(Point pos) {
            this.originalPosition = pos;
        }

        public void setIngredientName(String name) {
            this.ingredientName = name;
            // Load ingredient image
            loadIngredientImage();
        }

        public String getPotionType() {
            return potionType;
        }

        public void setPotionType(String type) {
            this.potionType = type;
        }

        public void setBoxVisible(boolean visible) {
            this.isBoxVisible = visible;
            repaint();
        }

        // Load ingredient image using actual filenames
        private void loadIngredientImage() {
            if (ingredientName == null) return;

            String imagePath = "/gameproject/resources/ingredients/" + ingredientName + ".png";
            ingredientImage = resourceManager.getImage(imagePath);

            // If image not found, try variations
            if (ingredientImage == null) {
                // Try with underscore prefix
                imagePath = "/gameproject/resources/ingredients/_" + ingredientName + ".png";
                ingredientImage = resourceManager.getImage(imagePath);

                // Try with underscore suffix if still not found
                if (ingredientImage == null) {
                    imagePath = "/gameproject/resources/ingredients/" + ingredientName + "_.png";
                    ingredientImage = resourceManager.getImage(imagePath);
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            // DO NOT call super.paintComponent() to avoid any background drawing
            // This is critical for complete transparency

             Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Only draw the grid box if it's visible AND we're in Phase 1
            if (isBoxVisible && currentPhase == 1) {
                if (gridBoxImage != null) {
                    g2d.drawImage(gridBoxImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Fallback colors if image is missing
                    Color boxColor;
                    // Alternate colors based on position to create a checkerboard effect
                    int row = getY() / INGREDIENT_SIZE;
                    int col = getX() / INGREDIENT_SIZE;
                    if ((row + col) % 2 == 0) {
                        boxColor = new Color(230, 195, 155); // Light tan
                    } else {
                        boxColor = new Color(215, 180, 140); // Darker tan
                    }
                    g2d.setColor(boxColor);
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                    // Draw border
                    g2d.setColor(new Color(165, 120, 95));
                    g2d.drawRect(0, 0, getWidth()-1, getHeight()-1);
                }
            }

            // Draw highlight if applicable - only in Phase 1
            if (isHighlighted && currentPhase == 1) {
                g2d.setColor(highlightColor);
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);
            }

            // Draw selection indicator - make this completely transparent in Phase 2
            if (isSelected && currentPhase != 2) {
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);
            }

            // Draw the ingredient image
            if (ingredientImage != null) {
                // Calculate size to maintain aspect ratio while ensuring the image is larger
                Image img = ingredientImage.getImage();
                int imgWidth = img.getWidth(this);
                int imgHeight = img.getHeight(this);

                // Skip if image hasn't loaded
                if (imgWidth <= 0 || imgHeight <= 0) return;

                // Scale image to fit within panel with small margins
                // Increased scale factor to make image larger
                double scale = Math.min(
                    (getWidth() - 10) / (double)imgWidth,
                    (getHeight() - 10) / (double)imgHeight
                );

                // Increase scale by 20% to make image slightly larger
                scale *= 1.2;

                int scaledWidth = (int)(imgWidth * scale);
                int scaledHeight = (int)(imgHeight * scale);

                // Center the image
                int x = (getWidth() - scaledWidth) / 2;
                int y = (getHeight() - scaledHeight) / 2;

                // Draw the image with nearest-neighbor interpolation for crisp pixel art
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                  RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2d.drawImage(img, x, y, scaledWidth, scaledHeight, this);
            }

            // For group labels (used in Phase 3), draw just text without any colored box
            if (isGroupLabel) {

                // Get text color based on potion type (kept for text only)
                Color textColor = Color.WHITE; // Default white text
                if (potionType.contains("Fire")) {
                    textColor = new Color(255, 80, 80); // Red for fire
                } else if (potionType.contains("Cold")) {
                    textColor = new Color(80, 80, 255); // Blue for cold
                } else if (potionType.contains("Strength")) {
                    textColor = new Color(255, 140, 0); // Orange for strength
                } else if (potionType.contains("Dexterity")) {
                    textColor = new Color(80, 200, 80); // Green for dexterity
                }

                // Draw potion name with appropriate color
                g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2d.setColor(textColor);

                // Split the potion name into lines if needed
                FontMetrics fm = g2d.getFontMetrics();
                String[] words = potionType.split(" ");
                int y = getHeight() / 2 - ((words.length * fm.getHeight()) / 2) + fm.getAscent();

                for (String word : words) {
                    int x = (getWidth() - fm.stringWidth(word)) / 2;
                    g2d.drawString(word, x, y);
                    y += fm.getHeight();
                }
            }
            
            // Only show value for regular ingredients, not group labels
            if (!isGroupLabel) {
                // Define circle size and position
                int circleSize = 28; // Size of the circle
                int circleX = getWidth() - circleSize - 5; // 5px from right edge
                int circleY = getHeight() - circleSize - 5; // 5px from bottom edge

                // Draw circle with semi-transparent background
                g2d.setColor(new Color(0, 0, 0, 180)); // Semi-transparent black
                g2d.fillOval(circleX, circleY, circleSize, circleSize);

                // Draw circle border
                g2d.setColor(new Color(255, 255, 255, 200)); // Semi-transparent white
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawOval(circleX, circleY, circleSize, circleSize);

                // Draw value text
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.setColor(Color.WHITE);

                // Center the text in the circle
                String valueText = String.valueOf(value);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = circleX + (circleSize - fm.stringWidth(valueText)) / 2;
                int textY = circleY + ((circleSize + fm.getAscent()) / 2);

                g2d.drawString(valueText, textX, textY);
            }
            
            
        }

        // Getters and setters
        public int getValue() {
            return value;
        }

        public String getIngredientName() {
            return ingredientName;
        }

        public String getColor() {
            return color;
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setHighlighted(boolean highlighted) {
            this.isHighlighted = highlighted;
            repaint();
        }

        public void setHighlightColor(Color color) {
            this.highlightColor = color;
            repaint();
        }

        public void setGroupLabel(boolean isGroupLabel) {
            this.isGroupLabel = isGroupLabel;
            repaint();
        }

        public boolean isGroupLabel() {
            return isGroupLabel;
        }
    }
    
    
    
    
    
    // Store the colors for each potion type
    private Color getColorForPotionType(String potionType) {
        switch (potionType) {
            case "Fire Resistance":
                return new Color(255, 80, 80); // Red
            case "Cold Resistance":
                return new Color(80, 80, 255); // Blue
            case "Strength":
                return new Color(255, 140, 0); // Orange
            case "Dexterity":
                return new Color(0, 200, 0);   // Green
            default:
                return Color.WHITE;            // Default color
        }
    }
    
 
    
    /**
    * Add this method to TimSortVisualization class to show phase dialogues
    */
    private void showPhaseDialogue(String baseDialogueKey) {
        // IMPORTANT: Clean up any active timers first
        stopAllTimers();
        
        
        
        
        // IMPORTANT: Remove any existing dialogue overlays first
        for (Component comp : getComponents()) {
            if (comp instanceof JPanel && comp.getName() != null && 
                comp.getName().equals("dialogueOverlay")) {
                remove(comp);
            }
        }

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
        dialogueOverlay.setName("dialogueOverlay");

        dialogueOverlay.setLayout(null);
        dialogueOverlay.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        dialogueOverlay.setOpaque(false);

        // Add overlay to the panel - always at index 0 (top)
        add(dialogueOverlay, 0);

        // IMPORTANT: Ensure the dialogue overlay is the topmost component
        setComponentZOrder(dialogueOverlay, 0);

        // Get the dialogue sequence from NarrativeSystem
        NarrativeSystem narrativeSystem = NarrativeSystem.getInstance();
        List<NarrativeSystem.DialogueEntry> dialogueSequence;

        // Apply game level prefix to base dialogue key if needed
        String dialogueKey = baseDialogueKey;
        if (gameLevel == 3 && !dialogueKey.startsWith("level3_")) {
            // Check if level3 version exists
            String level3Key = "level3_" + baseDialogueKey;
            if (narrativeSystem.getDialogueSequence(level3Key) != null) {
                dialogueKey = level3Key;
            }
        } else if (gameLevel == 2 && !dialogueKey.startsWith("level2_")) {
            // Check if level2 version exists
            String level2Key = "level2_" + baseDialogueKey;
            if (narrativeSystem.getDialogueSequence(level2Key) != null) {
                dialogueKey = level2Key;
            }
        }

        // Special handling for phase2_end to make it dynamic
        if (baseDialogueKey.equals("phase2_end") || 
            baseDialogueKey.equals("level2_phase2_end") || 
            baseDialogueKey.equals("level3_phase2_end")) {
            // Get dynamic dialogue using actual potion types detected in Phase 2
            dialogueSequence = narrativeSystem.getDynamicPhase2EndDialogue(
                leftGroupPotionType, rightGroupPotionType);
        } else {
            // Get regular dialogue sequence for other dialogue keys
            dialogueSequence = narrativeSystem.getDialogueSequence(dialogueKey);
        }

        if (dialogueSequence == null || dialogueSequence.isEmpty()) {
            // If no dialogue found, just remove the overlay
            remove(dialogueOverlay);
            repaint();
            return;
        }

        // Create a dialogue manager specifically for this overlay
        DialogueManager phaseDialogueManager = new DialogueManager(controller);
        phaseDialogueManager.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        dialogueOverlay.add(phaseDialogueManager);

        // Special handling for phase 3 end to sequence with battle
        final boolean isPhase3End = (currentPhase == 3 && 
            (baseDialogueKey.equals("phase3_end") || 
             baseDialogueKey.equals("level2_phase3_end") || 
             baseDialogueKey.equals("level3_phase3_end")));

        // Add a listener to remove the overlay when dialogue ends
        phaseDialogueManager.setDialogueEndListener(new DialogueManager.DialogueEndListener() {
            @Override
            public void onDialogueEnd() {
                // Remove the overlay when dialogue completes
                remove(dialogueOverlay);
                repaint();

                // Special handling for Phase 3 end dialogue
                if (isPhase3End && phaseCompleted) {
                    // Start appropriate boss battle based on game level
                    startBossBattle();
                } else {
                    // Resume any paused game elements if needed for other phases
                    resumeAfterDialogue();
                }
            }
        });

        // Start the dialogue sequence
        phaseDialogueManager.startDialogue(dialogueSequence);

        // Pause any active timers or animations during dialogue
        pauseDuringDialogue();

        // Force revalidate and repaint
        revalidate();
        repaint();
    }
    
    
    
    
    
    
    
    
    
    /**
    * Pause game elements during dialogue
    */
    private void pauseDuringDialogue() {


        // Disable buttons during dialogue
        abilityButton.setEnabled(false);
        checkButton.setEnabled(false);
        hintButton.setEnabled(false);
        pauseButton.setEnabled(false);
    }

    
    /**
    * Resume game elements after dialogue ends
    */
    private void resumeAfterDialogue() {


        // Re-enable buttons
        abilityButton.setEnabled(true);
        checkButton.setEnabled(currentPhase == 1 ? selectedIngredients.size() == MAX_SELECTIONS : true);
        hintButton.setEnabled(true);
        pauseButton.setEnabled(true);
    }
    
    
    /**
    * Get the left group potion type identified in Phase 2
    */
    public String getLeftGroupPotionType() {
        return leftGroupPotionType;
    }

    /**
     * Get the right group potion type identified in Phase 2
     */
    public String getRightGroupPotionType() {
        return rightGroupPotionType;
    }

    /**
     * Get the crafted potion selected in Phase 3
     */
    public String getCraftedPotion() {
        return craftedPotion;
    }
    
    
    /**
    * Reset all phases and state in the TimSort visualization
    */
    public void resetAllPhases() {
        // Reset phase tracking
        currentPhase = 1;
        phaseCompleted = false;

        // Reset to Eye of Pattern ability
        loadPhaseAbilityIcons(1);

        // Clear all data structures
        allIngredients.clear();
        selectedIngredients.clear();
        identifiedRuns.clear();
        leftGroup.clear();
        rightGroup.clear();
        mergedItems.clear();
        craftedPotion = null;

        // Clear UI
        gridPanel.removeAll();

        // Reset UI state
        updateAbilityButtonText("Use Eye of Pattern");
        instructionLabel.setText("Use your 'Eye of Pattern' ability to identify ingredient sequences (runs).");
        checkButton.setEnabled(false);


        // Force UI refresh
        revalidate();
        repaint();
    }
    
    
    /**
    * Reset all phases for Level 2
    */
    public void resetForLevel2() {
    // Set game level to 2
    gameLevel = 2;

    // Reset phase tracking
    currentPhase = 1;
    phaseCompleted = false;

    // Clear all data structures
    allIngredients.clear();
    selectedIngredients.clear();
    identifiedRuns.clear();
    leftGroup.clear();
    rightGroup.clear();
    mergedItems.clear();
    craftedPotion = null;

    // Set Toxitar as the current boss
    currentBossName = "Toxitar";

    // Clear UI
    gridPanel.removeAll();

    // Reset UI state
    abilityButton.setText("Use Eye of Pattern");
    instructionLabel.setText("Identify ingredients that enhance agility to evade Toxitar's poison.");
    checkButton.setEnabled(false);

    // Update phase label
    phaseLabel.setText("Phase 1: The Eye of Pattern (Toxitar)");

    // Update the hints for Toxitar
    toxitarHints = new String[] {
        "Look for ingredients with green coloring and light properties - these enhance agility.",
        "Sort each group from lightest to heaviest. The proper sequence is crucial for dexterity potions.",
        "Against poison that fills the air, quick movement is better than raw strength."
    };
    
    // CRITICAL FIX: Reset grid panel dimensions
    int cellSize = INGREDIENT_SIZE;
    int gridWidth = GRID_COLS * cellSize;
    int gridHeight = GRID_ROWS * cellSize;
    int GRID_PADDING = 12;
    int totalWidth = gridWidth + (GRID_PADDING * 2);
    int totalHeight = gridHeight + (GRID_PADDING * 2);
    int gridX = (GameConstants.WINDOW_WIDTH - totalWidth) / 2;
    int gridY = 135;
    gridPanel.setBounds(gridX, gridY, totalWidth, totalHeight);
    
    // Ensure the grid panel is visible and has null layout
    gridPanel.setLayout(null);
    gridPanel.setOpaque(false);
    gridPanel.setVisible(true);
    
    // CRITICAL FIX: Make sure ability button is enabled
    setAbilityButtonEnabled(true);
    System.out.println("DEBUG: Ability button enabled in resetForLevel2");

    // Force complete UI refresh before generating ingredients
    revalidate();
    repaint();
}
 
    
    
    
    /**
    * Reset all phases for Level 3
    */
    public void resetForLevel3() {
        // Set game level to 3
        gameLevel = 3;

        // Reset phase tracking
        currentPhase = 1;
        phaseCompleted = false;

        // Clear all data structures
        allIngredients.clear();
        selectedIngredients.clear();
        identifiedRuns.clear();
        leftGroup.clear();
        rightGroup.clear();
        mergedItems.clear();
        craftedPotion = null;

        // Set Lord Chaosa as the current boss
        currentBossName = "LordChaosa";

        // Clear UI
        gridPanel.removeAll();

        // Reset UI state
        abilityButton.setText("Use Eye of Pattern");
        instructionLabel.setText("Identify ingredients that enhance strength to counter Lord Chaosa's reality distortions.");
        checkButton.setEnabled(false);

        // Update phase label
        phaseLabel.setText("Phase 1: The Eye of Pattern (Lord Chaosa)");

        // Update the hints for Lord Chaosa
        lordChaosaHints = new String[] {
            "Look for ingredients with orange and brown coloring that enhance raw physical power.",
            "Sort each group from foundational to peak. The proper sequence builds power progressively.",
            "Against reality-warping chaos, only overwhelming physical force can break through the distortion."
        };

        // Force UI refresh
        revalidate();
        repaint();
    }
    
    
    
    
    
    
    
    public static class LevelProgressData {
        private int level;
        private int phase;
        private String leftPotionType;
        private String rightPotionType;

        public LevelProgressData(int level, int phase) {
            this.level = level;
            this.phase = phase;
        }

        public int getLevel() {
            return level;
        }

        public int getPhase() {
            return phase;
        }

        public String getLeftPotionType() {
            return leftPotionType;
        }

        public void setLeftPotionType(String leftPotionType) {
            this.leftPotionType = leftPotionType;
        }

        public String getRightPotionType() {
            return rightPotionType;
        }

        public void setRightPotionType(String rightPotionType) {
            this.rightPotionType = rightPotionType;
        }
    }
    
    
    
 
    // Modify the timer creation to track all timers
    private Timer createTrackedTimer(int delay, ActionListener listener) {
        Timer timer = new Timer(delay, listener);
        activeTimers.add(timer);
        return timer;
    }

    // Add a method to clean up timers
    private void stopAllTimers() {
        for (Timer timer : activeTimers) {
            if (timer.isRunning()) {
                timer.stop();
            }
        }
        activeTimers.clear();
    }
    
    
    
    
    // Add this method to completely reset the visualization's state
    public void fullReset() {
        // Stop all timers
        stopAllTimers();

        // Reset phase tracking
        currentPhase = 1;
        phaseCompleted = false;

        // Clear all data structures
        allIngredients.clear();
        selectedIngredients.clear();
        identifiedRuns.clear();
        leftGroup.clear();
        rightGroup.clear();
        mergedItems.clear();
        craftedPotion = null;

        // Reset UI state
        abilityButton.setText("Use Eye of Pattern");
        instructionLabel.setText("Use your 'Eye of Pattern' ability to identify ingredient sequences (runs).");
        checkButton.setEnabled(false);

        // Clear grid panel
        gridPanel.removeAll();

        // Force UI refresh
        revalidate();
        repaint();
    }

    
    
    
    
    // Add this method to load the appropriate background based on game level and phase
    private void loadBackground(int phase) {
        String backgroundPath;

        // Select background based on phase
        switch(phase) {
            case 1:
                backgroundPath = "/gameproject/resources/backgrounds/forest_bg.png";
                System.out.println("DEBUG: Loading Phase 1 background (forest)");
                break;
            case 2:
                backgroundPath = "/gameproject/resources/backgrounds/scholars_library_bg.png";
                System.out.println("DEBUG: Loading Phase 2 background (scholar's library)");
                break;
            case 3:
                backgroundPath = "/gameproject/resources/backgrounds/alchemy_laboratory_bg.png";
                System.out.println("DEBUG: Loading Phase 3 background (alchemy lab)");
                break;
            default:
                backgroundPath = "/gameproject/resources/backgrounds/forest_bg.png";
                System.out.println("DEBUG: Loading default background (forest)");
                break;
        }


        // Force reload by not using cache (for debugging)
        backgroundCache.remove(backgroundPath); // Remove from cache to force reload

        // Load the background image
        backgroundImage = resourceManager.getImage(backgroundPath);

        // Cache the background if it loaded successfully
        if (backgroundImage != null) {
            System.out.println("DEBUG: Successfully loaded background: " + backgroundPath);
            backgroundCache.put(backgroundPath, backgroundImage);
        } else {
            // If loading failed, create a fallback gradient
            System.out.println("WARNING: Failed to load background image: " + backgroundPath);

            // Create fallback gradient background
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
            backgroundCache.put(backgroundPath, backgroundImage);
        }
    }
    
//end of timsortvisualization class
}