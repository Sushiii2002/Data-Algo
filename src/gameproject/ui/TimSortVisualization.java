package gameproject.ui;

import gameproject.controller.GameController;
import gameproject.model.GameModel;
import gameproject.model.GameState;
import gameproject.util.ResourceManager;
import gameproject.util.GameConstants;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
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
    private JLabel timerLabel;
    private JButton pauseButton;
    private JPanel heartsPanel;
    private JLabel[] heartLabels;
    private Timer gameTimer;
    private int timeRemaining = 300; // 5 minutes in seconds
    private int livesRemaining = 3;
    private ImageIcon heartFilledIcon;
    private ImageIcon heartEmptyIcon;
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
        
        // Initialize timer to count down from 5 minutes
        gameTimer = new Timer(1000, e -> updateTimer());
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

        // Ability button
        abilityButton = createStyledButton("Use Eye of Pattern");
        abilityButton.addActionListener(e -> useAbility());
        controlPanel.add(abilityButton);

        // Check button
        checkButton = createStyledButton("Check Selection");
        checkButton.addActionListener(e -> checkPhaseCompletion());
        checkButton.setEnabled(false);
        controlPanel.add(checkButton);

        add(controlPanel);
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
        gameTimer.stop();

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
            // Resume the timer when Resume button is pressed
            gameTimer.start();
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
            remove(overlay);
            resetPhase();
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
            remove(overlay);
            controller.showMainMenu();
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
     * Reset the current phase
     */
    private void resetPhase() {
        // Reset timer
        timeRemaining = 300;
        updateTimerDisplay();
        
        // Reset lives
        resetLives();
        
        // Reset phase-specific state
        if (currentPhase == 1) {
            // Reset Eye of Pattern phase
            selectedIngredients.clear();
            identifiedRuns.clear();
            generateIngredients();
        } else if (currentPhase == 2) {
            // Reset Hand of Balance phase
            leftGroup.clear();
            rightGroup.clear();
            arrangeIngredientsForSorting();
        } else if (currentPhase == 3) {
            // Reset Mind of Unity phase
            craftedPotion = null;
            displayPotionOptions();
        }
        
        // Reset completion state
        phaseCompleted = false;
        checkButton.setEnabled(false);
        
        // Update UI
        revalidate();
        repaint();
    }
    
    /**
     * Reset lives to full
     */
    private void resetLives() {
        livesRemaining = 3;
        for (int i = 0; i < 3; i++) {
            heartLabels[i].setIcon(heartFilledIcon);
        }
    }
    
    /**
     * Lose a life and check if all hearts are gone
     */
    private void loseLife() {
        if (livesRemaining > 0) {
            livesRemaining--;
            heartLabels[livesRemaining].setIcon(heartEmptyIcon);

            // Check if all hearts are depleted
            if (livesRemaining == 0) {
                // Show level failed screen when all hearts are gone
                showLevelFailedScreen();
            }
        }
    }
    
    /**
     * Show level failed screen with semi-transparent dark overlay
     */
    private void showLevelFailedScreen() {
        // Stop the timer
        gameTimer.stop();

        // Create semi-transparent dark overlay panel
        JPanel overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Semi-transparent dark overlay (60% opacity black)
                g.setColor(new Color(0, 0, 0, 153));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlay.setLayout(null);
        overlay.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        overlay.setOpaque(false);
        add(overlay, 0);

        // Create "Level Failed" text
        JLabel failedLabel = new JLabel("Level", JLabel.CENTER);
        failedLabel.setFont(pixelifySansFont.deriveFont(50f));
        failedLabel.setForeground(Color.WHITE);
        failedLabel.setBounds(0, (GameConstants.WINDOW_HEIGHT / 2) - 80, GameConstants.WINDOW_WIDTH, 50);
        overlay.add(failedLabel);

        JLabel failedLabel2 = new JLabel("Failed", JLabel.CENTER);
        failedLabel2.setFont(pixelifySansFont.deriveFont(50f));
        failedLabel2.setForeground(Color.WHITE);
        failedLabel2.setBounds(0, (GameConstants.WINDOW_HEIGHT / 2) - 30, GameConstants.WINDOW_WIDTH, 50);
        overlay.add(failedLabel2);

        // Create buttons with same styling as pause menu
        AnimatedButton restartButton = new AnimatedButton("RESTART", 
            resourceManager.getImage("/gameproject/resources/NormalButton.png"),
            resourceManager.getImage("/gameproject/resources/HoverButton.png"),
            resourceManager.getImage("/gameproject/resources/ClickedButton.png"));
        restartButton.setFont(pixelifySansFont.deriveFont(28f));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBounds((GameConstants.WINDOW_WIDTH / 2) - 220, (GameConstants.WINDOW_HEIGHT / 2) + 50, 200, 60);
        restartButton.addActionListener(e -> {
            remove(overlay);
            resetPhase();
        });
        overlay.add(restartButton);

        AnimatedButton menuButton = new AnimatedButton("MAIN MENU", 
            resourceManager.getImage("/gameproject/resources/NormalButton.png"),
            resourceManager.getImage("/gameproject/resources/HoverButton.png"),
            resourceManager.getImage("/gameproject/resources/ClickedButton.png"));
        menuButton.setFont(pixelifySansFont.deriveFont(28f));
        menuButton.setForeground(Color.WHITE);
        menuButton.setBounds((GameConstants.WINDOW_WIDTH / 2) + 20, (GameConstants.WINDOW_HEIGHT / 2) + 50, 200, 60);
        menuButton.addActionListener(e -> {
            remove(overlay);
            controller.showMainMenu();
        });
        overlay.add(menuButton);

        revalidate();
        repaint();
    }
    
    /**
     * Generate ingredients for the grid
     */
    private void generateIngredients() {
        allIngredients.clear();
        selectedIngredients.clear();

        // Clear the grid panel
        gridPanel.removeAll();

        // Create sets of ingredients for each potion type with correct colors
        // Fire Resistance Potion (values 1-5)
        String[] fireIngredients = {"pumpkin", "apples", "peppers", "dragon_fire_glands", "fire_crystal"};
        for (int i = 0; i < 5; i++) {
            IngredientItem ingredient = new IngredientItem(i + 1, "red");
            ingredient.setIngredientName(fireIngredients[i]);
            ingredient.setPotionType("fire");
            // Set background color will be handled in paintComponent
            allIngredients.add(ingredient);
        }

        // Cold Resistance Potion (values 6-10)
        String[] coldIngredients = {"strawberries", "wasabi", "mint", "dragon_ice_glands", "ice_crystal"};
        for (int i = 0; i < 5; i++) {
            IngredientItem ingredient = new IngredientItem(i + 6, "blue");
            ingredient.setIngredientName(coldIngredients[i]);
            ingredient.setPotionType("cold");
            // Set background color will be handled in paintComponent
            allIngredients.add(ingredient);
        }

        // Strength Potion (values 11-15) - now with yellow background
        String[] strengthIngredients = {"corn", "powdered_giant_insect", "troll_sweat", "powdered_minotaur_horn", "dragon_bone"};
        for (int i = 0; i < 5; i++) {
            IngredientItem ingredient = new IngredientItem(i + 11, "yellow");
            ingredient.setIngredientName(strengthIngredients[i]);
            ingredient.setPotionType("strength");
            // Set background color will be handled in paintComponent
            allIngredients.add(ingredient);
        }

        // Dexterity Potion (values 16-20) - now with green background
        String[] dexterityIngredients = {"banana_leaf", "maple_sap", "powdered_jackalope_antlers", "griffon_feathers", "dragon_sinew"};
        for (int i = 0; i < 5; i++) {
            IngredientItem ingredient = new IngredientItem(i + 16, "green");
            ingredient.setIngredientName(dexterityIngredients[i]);
            ingredient.setPotionType("dexterity");
            // Set background color will be handled in paintComponent
            allIngredients.add(ingredient);
        }

        // Randomly position ingredients in the grid
        Collections.shuffle(allIngredients);
        positionIngredientsInGrid();

        // IMPORTANT: Make grid boxes visible by default without waiting for the ability
        for (IngredientItem ingredient : allIngredients) {
            ingredient.setBoxVisible(true);
        }

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
            // Phase 2: Drag to sort ingredients into groups
            moveIngredientToGroup(ingredient);
            
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
     * Move ingredient between groups in Phase 2
     */
    private void moveIngredientToGroup(IngredientItem ingredient) {
        if (!ingredient.isSelected()) {
            return;
        }
        
        // Determine target group
        if (leftGroup.contains(ingredient)) {
            // Move from left to right if right has space
            if (rightGroup.size() < GROUP_SIZE) {
                leftGroup.remove(ingredient);
                rightGroup.add(ingredient);
                
                // Sort right group
                sortGroup(rightGroup);
                updateGroupDisplay();
            }
        } else if (rightGroup.contains(ingredient)) {
            // Move from right to left if left has space
            if (leftGroup.size() < GROUP_SIZE) {
                rightGroup.remove(ingredient);
                leftGroup.add(ingredient);
                
                // Sort left group
                sortGroup(leftGroup);
                updateGroupDisplay();
            }
        } else {
            // Add to left group if it has space
            if (leftGroup.size() < GROUP_SIZE) {
                leftGroup.add(ingredient);
                sortGroup(leftGroup);
                updateGroupDisplay();
            } 
            // Otherwise add to right group if it has space
            else if (rightGroup.size() < GROUP_SIZE) {
                rightGroup.add(ingredient);
                sortGroup(rightGroup);
                updateGroupDisplay();
            }
        }
        
        // Check if both groups are full and sorted
        checkButton.setEnabled(leftGroup.size() == GROUP_SIZE && rightGroup.size() == GROUP_SIZE);
    }
    
    /**
    * Improved sort group method that does a thorough sort
    */
    private void sortGroup(List<IngredientItem> group) {
        // Sort using a simple and reliable approach - Java's built-in sort
        Collections.sort(group, (a, b) -> Integer.compare(a.getValue(), b.getValue()));
    }
    
    /**
     * Update the display of groups in Phase 2
     */
    private void updateGroupDisplay() {
        // Update left group display
        for (int i = 0; i < leftGroup.size(); i++) {
            IngredientItem ingredient = leftGroup.get(i);
            
            // Position in left group
            ingredient.setLocation(
                50 + (i * INGREDIENT_SIZE),
                250  // Moved from 200 to 250
            );
        }
        
        // Update right group display
        for (int i = 0; i < rightGroup.size(); i++) {
            IngredientItem ingredient = rightGroup.get(i);
            
            // Position in right group
            ingredient.setLocation(
                450 + (i * INGREDIENT_SIZE),
                250  // Moved from 200 to 250
            );
        }
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

        // Show confirmation
        JOptionPane.showMessageDialog(this,
            "You've selected the " + craftedPotion + "!",
            "Potion Selection",
            JOptionPane.INFORMATION_MESSAGE
        );

        // Enable check button
        checkButton.setEnabled(true);
        phaseCompleted = true;
    }
    
    /**
     * Use the current phase ability
     */
    private void useAbility() {
        if (currentPhase == 1) {
            // Eye of Pattern - Highlight runs
            highlightNaturalRuns();
        } else if (currentPhase == 2) {
            // Hand of Balance - Apply automatic sorting
            applyHandOfBalanceAbility();
        } else if (currentPhase == 3) {
            // Mind of Unity - Animate potion options display
            animatePotionOptions();
        }
    }
    
    
    /**
    * Animate the potion options appearing without any backgrounds or overlays
    */
    private void animatePotionOptions() {
        // First, disable ability button to prevent multiple animations
        abilityButton.setEnabled(false);

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

                    // Update instruction
                    instructionLabel.setText("Choose which potion to craft by clicking on it. Consider what would be most effective against Flameclaw.");

                    // Force refresh
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }
            }
        });

        // Start animation
        animationTimer.start();
    }

    
    
    /**
     * Highlight natural runs in the ingredient grid
     */
    private void highlightNaturalRuns() {
        identifiedRuns.clear();

        // Find runs for each potion type with corrected colors
        List<IngredientItem> fireRun = findRunByPotionType("fire");
        if (!fireRun.isEmpty()) {
            identifiedRuns.add(fireRun);
            highlightRun(fireRun, new Color(255, 50, 50, 80)); // Red highlight for fire
        }

        List<IngredientItem> coldRun = findRunByPotionType("cold");
        if (!coldRun.isEmpty()) {
            identifiedRuns.add(coldRun);
            highlightRun(coldRun, new Color(50, 50, 255, 80)); // Blue highlight for cold
        }

        List<IngredientItem> strengthRun = findRunByPotionType("strength");
        if (!strengthRun.isEmpty()) {
            identifiedRuns.add(strengthRun);
            highlightRun(strengthRun, new Color(255, 200, 50, 80)); // Yellow highlight for strength
        }

        List<IngredientItem> dexterityRun = findRunByPotionType("dexterity");
        if (!dexterityRun.isEmpty()) {
            identifiedRuns.add(dexterityRun);
            highlightRun(dexterityRun, new Color(50, 200, 50, 80)); // Green highlight for dexterity
        }

        // Update the instruction
        instructionLabel.setText("Natural runs highlighted! Select exactly 10 ingredients that form sequences.");

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

        // Make sure grid panel is on top of other components
        if (getComponentZOrder(gridPanel) > 0) {
            setComponentZOrder(gridPanel, 0);
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
    * Get the appropriate color for a potion type
    */
    private Color getPotionColor(String potionType) {
        switch (potionType) {
            case "Fire Resistance":
                return new Color(255, 80, 80); // Red
            case "Cold Resistance":
                return new Color(80, 80, 255); // Blue
            case "Strength":
                return new Color(255, 140, 0); // Orange
            case "Dexterity":
                return new Color(0, 200, 0); // Green
            default:
                return Color.WHITE; // Default color
        }
    }
    
    
    
    
    /**
    * Apply the Hand of Balance ability to sort ingredients
    */
    private void applyHandOfBalanceAbility() {
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

        // Create animation timer
        final int animationDuration = 2000; // 2 seconds
        final int fps = 60; // frames per second
        final int totalFrames = animationDuration / (1000 / fps);
        final int[] currentFrame = {0};

        Timer animationTimer = new Timer(1000 / fps, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentFrame[0]++;

                // Calculate animation progress (0.0 to 1.0)
                float progress = (float) currentFrame[0] / totalFrames;

                // Apply easing function (ease out)
                float easedProgress = 1.0f - (1.0f - progress) * (1.0f - progress);

                // Animate left group to their final positions (centered in row 2)
                for (int i = 0; i < leftGroup.size(); i++) {
                    IngredientItem ingredient = leftGroup.get(i);
                    if (ingredient.getOriginalPosition() == null) continue;

                    Point originalPos = ingredient.getOriginalPosition();
                    int targetX = startX1 + (i * (INGREDIENT_SIZE + 10));
                    int targetY = ingredientsY1;

                    // Calculate current position based on progress
                    int currentX = originalPos.x + (int)((targetX - originalPos.x) * easedProgress);
                    int currentY = originalPos.y + (int)((targetY - originalPos.y) * easedProgress);

                    ingredient.setLocation(currentX, currentY);

                    // Increase size slightly during animation
                    if (currentFrame[0] == 1) { // Only do this once at the start
                        ingredient.setSize(INGREDIENT_SIZE + 15, INGREDIENT_SIZE + 15);
                    }

                    // Ensure ingredient is visible by bringing it to front
                    gridPanel.setComponentZOrder(ingredient, 0);
                }

                // Animate right group to their final positions (centered in row 4)
                for (int i = 0; i < rightGroup.size(); i++) {
                    IngredientItem ingredient = rightGroup.get(i);
                    if (ingredient.getOriginalPosition() == null) continue;

                    Point originalPos = ingredient.getOriginalPosition();
                    int targetX = startX2 + (i * (INGREDIENT_SIZE + 10));
                    int targetY = ingredientsY2;

                    // Calculate current position based on progress
                    int currentX = originalPos.x + (int)((targetX - originalPos.x) * easedProgress);
                    int currentY = originalPos.y + (int)((targetY - originalPos.y) * easedProgress);

                    ingredient.setLocation(currentX, currentY);

                    // Increase size slightly during animation
                    if (currentFrame[0] == 1) { // Only do this once at the start
                        ingredient.setSize(INGREDIENT_SIZE + 15, INGREDIENT_SIZE + 15);
                    }

                    // Ensure ingredient is visible by bringing it to front
                    gridPanel.setComponentZOrder(ingredient, 0);
                }

                // If animation is complete
                if (currentFrame[0] >= totalFrames) {
                    ((Timer)e.getSource()).stop();

                    // Double-check sorting one more time for safety
                    if (!isGroupSorted(leftGroup) || !isGroupSorted(rightGroup)) {
                        // This is a fallback in case sorting somehow fails
                        System.err.println("WARNING: Groups still not sorted after animation!");

                        // Force resort - last chance fix
                        Collections.sort(leftGroup, (a, b) -> Integer.compare(a.getValue(), b.getValue()));
                        Collections.sort(rightGroup, (a, b) -> Integer.compare(a.getValue(), b.getValue()));

                        // Set flags regardless - we've done our best
                        isLeftGroupSorted = true;
                        isRightGroupSorted = true;
                    }

                    // Enable check button
                    checkButton.setEnabled(true);

                    // Update instructional text
                    instructionLabel.setText("The ingredients have been sorted. Check your results.");

                    // Force repaint to ensure everything is visible
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }

                // Repaint
                gridPanel.repaint();
            }
        });

        // Start animation
        animationTimer.start();
    }
    
    
    
    /**
    * Display potion options in Phase 3 based on potions identified in Phase 2
    */
    private void displayPotionOptions() {
        // Clear the grid panel
        gridPanel.removeAll();

        // Define the potion image size (increased by another 50 pixels)
        final int POTION_IMAGE_SIZE = 220;            // Now 220x220 pixels
        final int LABEL_HEIGHT = 25;                 // Height for title text
        final int DESC_HEIGHT = 60;                  // Height for description text
        final int PADDING_BELOW_IMAGE = 20;          // Space between image and title
        final int PADDING_BELOW_TITLE = 10;          // Space between title and description

        // Calculate vertical positions with proper spacing
        final int IMAGE_Y = 180;                     // Starting position for potion images
        final int TITLE_Y = IMAGE_Y + POTION_IMAGE_SIZE + PADDING_BELOW_IMAGE;  // Position for titles
        final int DESC_Y = TITLE_Y + LABEL_HEIGHT + PADDING_BELOW_TITLE;        // Position for descriptions

        // DYNAMIC POTION DETERMINATION - Based on selected ingredients from Phase 1
        // Count ingredients by type to determine which potions to show
        Map<String, Integer> potionTypeCounts = new HashMap<>();
        potionTypeCounts.put("fire", 0);
        potionTypeCounts.put("cold", 0);
        potionTypeCounts.put("strength", 0);
        potionTypeCounts.put("dexterity", 0);

        // Analyze selected ingredients from Phase 1
        for (IngredientItem ingredient : selectedIngredients) {
            int value = ingredient.getValue();
            String potionType;

            // Determine potion type based on ingredient value
            if (value >= 1 && value <= 5) {
                potionType = "fire";
            } else if (value >= 6 && value <= 10) {
                potionType = "cold";
            } else if (value >= 11 && value <= 15) {
                potionType = "strength";
            } else if (value >= 16 && value <= 20) {
                potionType = "dexterity";
            } else {
                potionType = "unknown";
            }

            // Increment count for this potion type
            potionTypeCounts.put(potionType, potionTypeCounts.getOrDefault(potionType, 0) + 1);
        }

        // Determine the two most common potion types
        List<Map.Entry<String, Integer>> sortedCounts = new ArrayList<>(potionTypeCounts.entrySet());
        sortedCounts.sort((a, b) -> b.getValue() - a.getValue()); // Sort in descending order

        // Default to fire and strength if no ingredients were selected
        String leftPotionType = "fire";
        String rightPotionType = "strength";

        // Set potion types based on most common ingredients
        if (sortedCounts.size() >= 1 && sortedCounts.get(0).getValue() > 0) {
            leftPotionType = sortedCounts.get(0).getKey();
        }
        if (sortedCounts.size() >= 2 && sortedCounts.get(1).getValue() > 0) {
            rightPotionType = sortedCounts.get(1).getKey();
        } else if (sortedCounts.size() >= 1 && sortedCounts.get(0).getValue() > 0) {
            // If only one type had ingredients, use the second most common
            for (String type : potionTypeCounts.keySet()) {
                if (!type.equals(leftPotionType)) {
                    rightPotionType = type;
                    break;
                }
            }
        }

        // Define potion information lookup
        Map<String, String[]> potionInfo = new HashMap<>();
        potionInfo.put("fire", new String[]{"Fire Resistance Potion", 
                                           "Protects against fire attacks and extreme heat.",
                                           "/gameproject/resources/potions/fire_resistance_potion.png",
                                           "RED"});  // RED for Fire Resistance
        potionInfo.put("cold", new String[]{"Cold Resistance Potion", 
                                           "Protects against ice attacks and freezing temperatures.",
                                           "/gameproject/resources/potions/cold_resistance_potion.png",
                                           "CYAN"}); // CYAN for Cold Resistance
        potionInfo.put("strength", new String[]{"Strength Potion", 
                                              "Enhances physical strength and combat abilities.",
                                              "/gameproject/resources/potions/strength_potion.png",
                                              "YELLOW"}); // YELLOW for Strength
        potionInfo.put("dexterity", new String[]{"Dexterity Potion", 
                                               "Improves agility, reflexes, and movement speed.",
                                               "/gameproject/resources/potions/dexterity_potion.png",
                                               "GREEN"});

        // Get position for left potion
        int leftX = GameConstants.WINDOW_WIDTH / 4 - (POTION_IMAGE_SIZE / 2);

        // Create invisible components for the left potion
        // 1. Get potion info
        String[] leftPotionData = potionInfo.get(leftPotionType);
        String leftPotionName = leftPotionData[0];
        String leftPotionDesc = leftPotionData[1];
        String leftPotionImgPath = leftPotionData[2];
        Color leftPotionColor = getColorForName(leftPotionData[3]);

        // 2. Create potion image label
        ImageIcon leftPotionImg = resourceManager.getImage(leftPotionImgPath);
        if (leftPotionImg != null) {
            JLabel leftImgLabel = new JLabel(new ImageIcon(leftPotionImg.getImage().getScaledInstance(
                    POTION_IMAGE_SIZE, POTION_IMAGE_SIZE, Image.SCALE_SMOOTH)));
            leftImgLabel.setBounds(leftX, IMAGE_Y, POTION_IMAGE_SIZE, POTION_IMAGE_SIZE);
            leftImgLabel.setVisible(false); // Initially invisible
            gridPanel.add(leftImgLabel);
        }

        // 3. Create potion title label
        JLabel leftTitleLabel = new JLabel(leftPotionName, JLabel.CENTER);
        leftTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        leftTitleLabel.setForeground(leftPotionColor);
        leftTitleLabel.setBounds(leftX - 65, TITLE_Y, POTION_IMAGE_SIZE + 130, LABEL_HEIGHT); 
        leftTitleLabel.setVisible(false); // Initially invisible
        gridPanel.add(leftTitleLabel);

        // 4. Create potion description
        JTextArea leftDescLabel = new JTextArea(leftPotionDesc);
        leftDescLabel.setEditable(false);
        leftDescLabel.setWrapStyleWord(true);
        leftDescLabel.setLineWrap(true);
        leftDescLabel.setOpaque(false);
        leftDescLabel.setForeground(Color.WHITE);
        leftDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        leftDescLabel.setBounds(leftX - 65, DESC_Y, POTION_IMAGE_SIZE + 130, DESC_HEIGHT);
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
        Color rightPotionColor = getColorForName(rightPotionData[3]);

        // 2. Create potion image label
        ImageIcon rightPotionImg = resourceManager.getImage(rightPotionImgPath);
        if (rightPotionImg != null) {
            JLabel rightImgLabel = new JLabel(new ImageIcon(rightPotionImg.getImage().getScaledInstance(
                    POTION_IMAGE_SIZE, POTION_IMAGE_SIZE, Image.SCALE_SMOOTH)));
            rightImgLabel.setBounds(rightX, IMAGE_Y, POTION_IMAGE_SIZE, POTION_IMAGE_SIZE);
            rightImgLabel.setVisible(false); // Initially invisible
            gridPanel.add(rightImgLabel);
        }

        // 3. Create potion title label
        JLabel rightTitleLabel = new JLabel(rightPotionName, JLabel.CENTER);
        rightTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        rightTitleLabel.setForeground(rightPotionColor);
        rightTitleLabel.setBounds(rightX - 65, TITLE_Y, POTION_IMAGE_SIZE + 130, LABEL_HEIGHT);
        rightTitleLabel.setVisible(false); // Initially invisible
        gridPanel.add(rightTitleLabel);

        // 4. Create potion description
        JTextArea rightDescLabel = new JTextArea(rightPotionDesc);
        rightDescLabel.setEditable(false);
        rightDescLabel.setWrapStyleWord(true);
        rightDescLabel.setLineWrap(true);
        rightDescLabel.setOpaque(false);
        rightDescLabel.setForeground(Color.WHITE);
        rightDescLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rightDescLabel.setBounds(rightX - 65, DESC_Y, POTION_IMAGE_SIZE + 130, DESC_HEIGHT);
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

        // Update instruction
        instructionLabel.setText("Use your 'Mind of Unity' to choose which potion to craft. Click the button below.");

        // Enable ability button
        abilityButton.setText("Use Mind of Unity");
        abilityButton.setEnabled(true);

        // Disable check button until a potion is selected
        checkButton.setEnabled(false);

        // Add click listeners to potions for selection
        leftPotionItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (leftPotionItem.isVisible()) {
                    selectPotionGroup(1); // Left potion

                    // Store the selected potion name
                    craftedPotion = leftPotionName;
                }
            }
        });

        rightPotionItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (rightPotionItem.isVisible()) {
                    selectPotionGroup(2); // Right potion

                    // Store the selected potion name
                    craftedPotion = rightPotionName;
                }
            }
        });

        // Update UI
        gridPanel.revalidate();
        gridPanel.repaint();
    }
    
    
    
    
    
    /**
    * Helper method to convert color name string to Color object
    */
    private Color getColorForName(String colorName) {
        switch (colorName.toUpperCase()) {
            case "RED": return Color.RED;
            case "GREEN": return new Color(0, 180, 0);  // Brighter green
            case "BLUE": return Color.BLUE;
            case "CYAN": return Color.CYAN;
            case "YELLOW": return new Color(255, 215, 0);  // Gold yellow
            case "ORANGE": return new Color(255, 165, 0);  // Orange
            case "PURPLE": return new Color(128, 0, 128);  // Purple
            default: return Color.WHITE;
        }
    }
    
    
    
    /**
    * Modified check phase completion to account for automatic sorting
    */
    private void checkPhaseCompletion() {
        if (currentPhase == 1) {
            // Phase 1 code unchanged
            if (selectedIngredients.size() == MAX_SELECTIONS) {
                boolean hasValidRuns = checkValidRuns();

                if (hasValidRuns) {
                    JOptionPane.showMessageDialog(this,
                        "Well done! Your Eye of Pattern has identified natural sequences!",
                        "Phase 1 Complete",
                        JOptionPane.INFORMATION_MESSAGE
                    );

                    phaseCompleted = true;
                    Timer transitionTimer = new Timer(1500, e -> {
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
                    loseLife();
                }
            }
        } else if (currentPhase == 2) {
            // Check if both groups are actually sorted - using our verification method
            boolean leftSorted = isGroupSorted(leftGroup);
            boolean rightSorted = isGroupSorted(rightGroup);

            // Debug output
            System.out.println("Checking Phase 2 completion:");
            System.out.println("Left group sorted: " + leftSorted + ", flag: " + isLeftGroupSorted);
            System.out.println("Right group sorted: " + rightSorted + ", flag: " + isRightGroupSorted);
            System.out.println("Left group size: " + leftGroup.size() + ", Right group size: " + rightGroup.size());
            System.out.println("Left group potion type: " + leftGroupPotionType);
            System.out.println("Right group potion type: " + rightGroupPotionType);

            // Now check if the ability has been used AND the groups are sorted
            if (isLeftGroupSorted && isRightGroupSorted && leftSorted && rightSorted) {
                JOptionPane.showMessageDialog(this,
                    "Well done! Your Hand of Balance has arranged the ingredients perfectly!",
                    "Phase 2 Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );

                phaseCompleted = true;
                Timer transitionTimer = new Timer(1500, e -> {
                    advanceToNextPhase();
                });
                transitionTimer.setRepeats(false);
                transitionTimer.start();
            } else if (!isLeftGroupSorted || !isRightGroupSorted) {
                // They haven't used the ability yet
                JOptionPane.showMessageDialog(this,
                    "Use the 'Hand of Balance' ability first to sort the ingredients!",
                    "Try Again",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                // This should rarely happen - something is wrong with the sorting
                JOptionPane.showMessageDialog(this,
                    "The ingredients aren't properly sorted yet. Try again!",
                    "Try Again",
                    JOptionPane.WARNING_MESSAGE
                );
                loseLife();

                // Try to recover - reset the flags to force them to use the ability again
                isLeftGroupSorted = false;
                isRightGroupSorted = false;
            }
        } else if (currentPhase == 3) {
            // Phase 3 - now using the dynamic dialogue
            if (craftedPotion != null) {
                System.out.println("DEBUG: Starting boss battle with crafted potion: " + craftedPotion);
                System.out.println("DEBUG: Left potion type: " + leftGroupPotionType);
                System.out.println("DEBUG: Right potion type: " + rightGroupPotionType);

                startBossBattle("Flameclaw");
            }
        }
    }
    
    
    
    
    /**
    * Display boss battle and determine the outcome after animation
    * WITH DYNAMIC MESSAGES based on the actual selected potion
    */
    private void startBossBattle(String bossName) {
        // Add debug logging
        System.out.println("DEBUG: Starting boss battle against " + bossName);
        System.out.println("DEBUG: Selected potion: " + craftedPotion);
        System.out.println("DEBUG: Effective potions: [" + leftGroupPotionType + " Potion, " + rightGroupPotionType + " Potion]");

        // Create semi-transparent overlay
        JPanel battleOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Dark battle background
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        battleOverlay.setLayout(null);
        battleOverlay.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        battleOverlay.setOpaque(false);
        add(battleOverlay, 0);

        // Load boss image
        String bossImagePath = "/gameproject/resources/characters/" + bossName.toLowerCase() + ".png";
        ImageIcon bossImage = resourceManager.getImage(bossImagePath);

        if (bossImage != null) {
            // Scale boss image
            Image scaledBossImage = bossImage.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            JLabel bossLabel = new JLabel(new ImageIcon(scaledBossImage));
            bossLabel.setBounds((GameConstants.WINDOW_WIDTH - 300) / 2, 100, 300, 300);
            battleOverlay.add(bossLabel);

            // Add boss name
            JLabel bossNameLabel = new JLabel(bossName, JLabel.CENTER);
            bossNameLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
            bossNameLabel.setForeground(Color.RED);
            bossNameLabel.setBounds(0, 420, GameConstants.WINDOW_WIDTH, 40);
            battleOverlay.add(bossNameLabel);

            // Add battle text
            JLabel battleText = new JLabel("Battle in progress...", JLabel.CENTER);
            battleText.setFont(new Font("SansSerif", Font.BOLD, 24));
            battleText.setForeground(Color.WHITE);
            battleText.setBounds(0, 470, GameConstants.WINDOW_WIDTH, 30);
            battleOverlay.add(battleText);

            // Add animated battle effects
            startBattleEffects(battleOverlay);
        }

        // After a delay, show battle outcome
        Timer battleTimer = new Timer(5000, e -> {
            remove(battleOverlay);

            // Extract the actual potion type name from the craftedPotion string (remove " Potion" suffix)
            String selectedPotionType = craftedPotion;
            if (selectedPotionType.endsWith(" Potion")) {
                selectedPotionType = selectedPotionType.substring(0, selectedPotionType.length() - 7);
            }
            System.out.println("DEBUG: Extracted potion type: " + selectedPotionType);

            // Determine the correct potion to use against this boss
            boolean correctChoice = false;

            // For Flameclaw, the correct choice is Fire Resistance
            if (bossName.equals("Flameclaw")) {
                correctChoice = selectedPotionType.equals("Fire Resistance");
                System.out.println("DEBUG: Correct choice for Flameclaw is Fire Resistance, selected: " + selectedPotionType);
            }
            // For Toxitar, the correct choice would be something else
            else if (bossName.equals("Toxitar")) {
                correctChoice = selectedPotionType.equals("Cold Resistance");
                System.out.println("DEBUG: Correct choice for Toxitar is Cold Resistance, selected: " + selectedPotionType);
            }
            // Add more boss conditions as needed

            if (correctChoice) {
                JOptionPane.showMessageDialog(this,
                    "Excellent choice! The " + selectedPotionType + " Potion protected you from " + bossName + "'s attacks!",
                    "Success!",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Signal successful boss battle
                controller.onBossBattleComplete(true, 1);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Oh no! The " + selectedPotionType + " Potion wasn't effective against " + bossName + "'s flames!",
                    "Failure",
                    JOptionPane.WARNING_MESSAGE
                );

                // Signal failed boss battle
                controller.onBossBattleComplete(false, 1);
            }
        });
        battleTimer.setRepeats(false);
        battleTimer.start();
    }
    
    
    
    

    private void startBattleEffects(JPanel overlay) {
        // Create animated battle effects
        Random rand = new Random();

        // Create flame/spark effects for a fire boss
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
                    Color color = new Color(
                        rand.nextInt(100) + 155, 
                        rand.nextInt(100), 
                        rand.nextInt(50),
                        rand.nextInt(100) + 155);
                    int size = rand.nextInt(20) + 10;

                    @Override
                    protected void paintComponent(Graphics g) {
                        g.setColor(color);
                        g.fillOval(0, 0, size, size);
                    }
                };

                // Set random position around boss
                int x = (GameConstants.WINDOW_WIDTH / 2) + rand.nextInt(300) - 150;
                int y = 250 + rand.nextInt(200) - 100;
                effect.setBounds(x, y, 30, 30);
                effect.setOpaque(false);

                overlay.add(effect);
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
        currentPhase++;
        phaseCompleted = false;

        // Reset everything - crucial for clean transition
        gridPanel.removeAll();

        // Update phase label
        if (currentPhase == 2) {
            phaseLabel.setText("Phase 2: The Hand of Balance");
            abilityButton.setText("Use Hand of Balance");

            // Update the controller about the phase change for dynamic dialogue
            controller.onPhaseAdvance(currentPhase, leftGroupPotionType, rightGroupPotionType);

            // IMPORTANT: Use arrangeIngredientsForSorting instead of useAbility
            arrangeIngredientsForSorting();
        } else if (currentPhase == 3) {
            phaseLabel.setText("Phase 3: The Mind of Unity");
            abilityButton.setText("Use Mind of Unity");

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
     * Show a hint for the current phase
     */
    private void showHint() {
        String hint = "";
        
        if (currentPhase == 1) {
            hint = "Look for ingredients of the same color. They often form natural sequences when arranged by value. Blue ingredients resist fire!";
        } else if (currentPhase == 2) {
            hint = "Sort each group from lowest to highest value. The frost ingredients (left group) will be crucial for your potion.";
        } else if (currentPhase == 3) {
            hint = "Consider what would counter Flameclaw's fire. Resistance is more effective than raw strength against elemental attacks.";
        }
        
        JOptionPane.showMessageDialog(this,
            hint,
            "Hint",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Update the timer - counting down from 5 minutes
     */
    private void updateTimer() {
        if (timeRemaining > 0) {
            timeRemaining--;
            updateTimerDisplay();
            
            // Check if time is up
            if (timeRemaining <= 0) {
                gameTimer.stop();
                showLevelFailedScreen();
            }
        }
    }
    
    /**
     * Update the timer display
     */
    private void updateTimerDisplay() {
       int minutes = timeRemaining / 60;
       int seconds = timeRemaining % 60;
       timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }
    
    /**
     * Start the timer for this phase
     */
    public void startTimer() {
        timeRemaining = 300; // Reset to 5 minutes
        updateTimerDisplay();
        gameTimer.start();
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
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background
        g.setColor(new Color(25, 25, 50));
        g.fillRect(0, 0, getWidth(), getHeight());

        // No phase-specific decorative elements - removed
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
            // First reset everything
            gridPanel.removeAll();

            // Set the new phase
            currentPhase = phase;

            // Update phase label
            updatePhaseLabel();

            // Then initialize the proper phase UI
            initializePhaseUI();
        }
    }

   /**

    * Update the phase label based on current phase

    */

    private void updatePhaseLabel() {

        switch (currentPhase) {

            case 1:

                phaseLabel.setText("Phase 1: The Eye of Pattern");

                break;

            case 2:

                phaseLabel.setText("Phase 2: The Hand of Balance");

                break;

            case 3:

                phaseLabel.setText("Phase 3: The Mind of Unity");

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
        

        // Set button labels
        switch (currentPhase) {
            case 1:
                abilityButton.setText("Use Eye of Pattern");
                instructionLabel.setText("Use your 'Eye of Pattern' ability to identify ingredient sequences (runs).");
                // Generate ingredients for Phase 1
                generateIngredients();
                break;

            case 2:
                abilityButton.setText("Use Hand of Balance");
                instructionLabel.setText("Use your 'Hand of Balance' to sort ingredients into two groups.");
                // Setup Phase 2 with scattered ingredients
                arrangeIngredientsForSorting();
                break;

            case 3:
                abilityButton.setText("Use Mind of Unity");
                instructionLabel.setText("Use your 'Mind of Unity' ability to reveal the potions you can craft.");
                // Set up Phase 3 - but don't display potions yet
                displayPotionOptions();
                // Enable the ability button explicitly
                abilityButton.setEnabled(true);
                break;
        }



        // Reset state
        checkButton.setEnabled(false);
        phaseCompleted = false;

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
                // MODIFIED: No more colored backgrounds for potion labels
                // Just draw the potion name text directly

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
    * Get the description for a specific potion type
    */
    private String getPotionDescription(String potionType) {
        switch (potionType) {
            case "Fire Resistance":
                return "Protects against fire attacks and extreme heat.";
            case "Cold Resistance":
                return "Protects against ice attacks and freezing temperatures.";
            case "Strength":
                return "Enhances physical strength and combat abilities.";
            case "Dexterity":
                return "Improves agility, reflexes, and precision.";
            default:
                return "A mysterious potion with unknown effects.";
        }
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
    
//end of timsortvisualization class
}