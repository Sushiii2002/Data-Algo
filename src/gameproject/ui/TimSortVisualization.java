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
import java.util.List;
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

        // Create sets of ingredients for each potion type
        // Fire Resistance Potion (values 1-5)
        String[] fireIngredients = {"pumpkin", "apples", "peppers", "dragon_fire_glands", "fire_crystal"};
        for (int i = 0; i < 5; i++) {
            IngredientItem ingredient = new IngredientItem(i + 1, "red");
            ingredient.setIngredientName(fireIngredients[i]);
            ingredient.setPotionType("fire");
            allIngredients.add(ingredient);
        }

        // Cold Resistance Potion (values 6-10)
        String[] coldIngredients = {"strawberries", "wasabi", "mint", "dragon_ice_glands", "ice_crystal"};
        for (int i = 0; i < 5; i++) {
            IngredientItem ingredient = new IngredientItem(i + 6, "blue");
            ingredient.setIngredientName(coldIngredients[i]);
            ingredient.setPotionType("cold");
            allIngredients.add(ingredient);
        }

        // Strength Potion (values 11-15)
        String[] strengthIngredients = {"corn", "powdered_giant_insect", "troll_sweat", "powdered_minotaur_horn", "dragon_bone"};
        for (int i = 0; i < 5; i++) {
            IngredientItem ingredient = new IngredientItem(i + 11, "green");
            ingredient.setIngredientName(strengthIngredients[i]);
            ingredient.setPotionType("strength");
            allIngredients.add(ingredient);
        }

        // Dexterity Potion (values 16-20)
        String[] dexterityIngredients = {"banana_leaf", "maple_sap", "powdered_jackalope_antlers", "griffon_feathers", "dragon_sinew"};
        for (int i = 0; i < 5; i++) {
            IngredientItem ingredient = new IngredientItem(i + 16, "yellow");
            ingredient.setIngredientName(dexterityIngredients[i]);
            ingredient.setPotionType("dexterity");
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
     * Add a run of ingredients with sequential values
     */
    private void addIngredientsRun(int startValue, int endValue, String color) {
        for (int value = startValue; value <= endValue; value++) {
            IngredientItem ingredient = new IngredientItem(value, color);
            allIngredients.add(ingredient);
        }
    }
    
    /**
     * Add random ingredients to fill the grid
     */
    private void addRandomIngredients(int count) {
        String[] colors = {"blue", "red", "green", "yellow", "purple"};
        
        for (int i = 0; i < count; i++) {
            int value = 13 + i;
            String color = colors[(int)(Math.random() * colors.length)];
            IngredientItem ingredient = new IngredientItem(value, color);
            allIngredients.add(ingredient);
        }
    }
    
    /**
     * Position ingredients randomly in the grid
     */
    private void positionIngredientsRandomly() {
        // Shuffle the ingredients list for random placement
        Collections.shuffle(allIngredients);

        for (int i = 0; i < allIngredients.size(); i++) {
            IngredientItem ingredient = allIngredients.get(i);

            // Calculate grid positions 
            int row = i / GRID_COLS;
            int col = i % GRID_COLS;

            // Position in grid
            ingredient.setBounds(
                col * INGREDIENT_SIZE,
                row * INGREDIENT_SIZE,
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
     * Sort a group of ingredients (for visual feedback)
     */
    private void sortGroup(List<IngredientItem> group) {
        // Simple bubble sort for demonstration
        for (int i = 0; i < group.size() - 1; i++) {
            for (int j = 0; j < group.size() - 1 - i; j++) {
                if (group.get(j).getValue() > group.get(j + 1).getValue()) {
                    // Swap
                    IngredientItem temp = group.get(j);
                    group.set(j, group.get(j + 1));
                    group.set(j + 1, temp);
                }
            }
        }
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
        if (groupId == 1) {
            craftedPotion = "Fire Resistance Potion";
        } else {
            craftedPotion = "Strength Potion";
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
            // Mind of Unity - Display potion options
            displayPotionOptions();
        }
    }
    
    /**
     * Highlight natural runs in the ingredient grid
     */
    private void highlightNaturalRuns() {
        identifiedRuns.clear();

        // Find runs for each potion type
        List<IngredientItem> fireRun = findRunByPotionType("fire");
        if (!fireRun.isEmpty()) {
            identifiedRuns.add(fireRun);
            highlightRun(fireRun, new Color(255, 50, 50, 80)); // Red highlight
        }

        List<IngredientItem> coldRun = findRunByPotionType("cold");
        if (!coldRun.isEmpty()) {
            identifiedRuns.add(coldRun);
            highlightRun(coldRun, new Color(50, 50, 255, 80)); // Blue highlight
        }

        List<IngredientItem> strengthRun = findRunByPotionType("strength");
        if (!strengthRun.isEmpty()) {
            identifiedRuns.add(strengthRun);
            highlightRun(strengthRun, new Color(50, 200, 50, 80)); // Green highlight
        }

        List<IngredientItem> dexterityRun = findRunByPotionType("dexterity");
        if (!dexterityRun.isEmpty()) {
            identifiedRuns.add(dexterityRun);
            highlightRun(dexterityRun, new Color(255, 200, 50, 80)); // Yellow highlight
        }

        // No need to make grid boxes visible here since they're visible by default now
        // Just update the instruction
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
    * Arrange ingredients for sorting in Phase 2 - Revised version
    */
    private void arrangeIngredientsForSorting() {
    // DEBUGGING - add a console message to verify this method is actually running
    System.out.println("PHASE 2: arrangeIngredientsForSorting is running");

    // Clear groups
    leftGroup.clear();
    rightGroup.clear();

    // Clear the grid panel - COMPLETELY REMOVE ALL ELEMENTS
    gridPanel.removeAll();

    // Make sure grid panel has no background
    gridPanel.setBackground(new Color(0, 0, 0, 0));
    gridPanel.setOpaque(false);

    // CRITICAL FIX: Move grid panel to cover entire screen instead of being centered
    // This allows absolute positioning of ingredients anywhere on screen
    gridPanel.setBounds(0, 0, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);

    // Update ability button text and instruction
    abilityButton.setText("Use Hand of Balance");

    // Start with the right instruction that doesn't claim sorting is done
    instructionLabel.setText("Use your 'Hand of Balance' to sort ingredients into two groups.");

    checkButton.setEnabled(false);

    // Create group headers (initially invisible) - use absolute positions now
    JLabel leftHeader = new JLabel("Frost Ingredients", JLabel.CENTER);
    leftHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
    leftHeader.setForeground(Color.CYAN);
    leftHeader.setBounds(150, 200, 300, 30); // Absolute position
    leftHeader.setVisible(false); // Initially invisible
    gridPanel.add(leftHeader);

    JLabel rightHeader = new JLabel("Power Ingredients", JLabel.CENTER);
    rightHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
    rightHeader.setForeground(Color.RED);
    rightHeader.setBounds(550, 200, 300, 30); // Absolute position
    rightHeader.setVisible(false); // Initially invisible
    gridPanel.add(rightHeader);

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

    // FIXED: Use absolute positions that are truly to the left side of the screen
    // These are now absolute screen coordinates, not relative to grid
    int[][] positions = new int[][] {
        // Left side positions (5 ingredients) - TRULY LEFT
        {50, 180}, {100, 150}, {150, 210}, {200, 240}, {250, 270},
        // Right side positions (5 ingredients) - CENTER-RIGHT
        {550, 180}, {600, 150}, {650, 210}, {700, 240}, {750, 270}
    };

    // Assign ingredients to groups and position them
    for (int i = 0; i < ingredientsToUse.size() && i < positions.length; i++) {
        IngredientItem ingredient = ingredientsToUse.get(i);
        
        // Reset visual state
        ingredient.setSelected(false);
        ingredient.setHighlighted(false);
        ingredient.setBoxVisible(false);
        
        // Add to appropriate group (first 5 to left, next 5 to right)
        if (i < 5) {
            leftGroup.add(ingredient);
        } else if (i < 10) {
            rightGroup.add(ingredient);
        }
        
        // Position based on predefined coordinates - now absolute
        int posX = positions[i][0];
        int posY = positions[i][1];
        
        // Store original position for animation
        ingredient.setOriginalPosition(new Point(posX, posY));
        ingredient.setLocation(posX, posY);
        
        // Add to grid panel
        gridPanel.add(ingredient);
        
        // Add click listeners for item interaction
        ingredient.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleIngredientClick(ingredient);
            }
        });
    }

    // Sort the groups (not visible yet)
    sortGroup(leftGroup);
    sortGroup(rightGroup);

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
    * Apply the Hand of Balance ability to sort ingredients
    */
    private void applyHandOfBalanceAbility() {
    // First, show the group headers
    for (Component c : gridPanel.getComponents()) {
        if (c instanceof JLabel && !(c instanceof IngredientItem)) {
            c.setVisible(true);
        }
    }

    // Disable the ability button during animation
    abilityButton.setEnabled(false);

    // FIXED: Use absolute coordinates that match the headers
    int leftGroupStartX = 150;  // Match left header
    int rightGroupStartX = 550; // Match right header
    int groupsY = 250;
    int columnWidth = INGREDIENT_SIZE + 10; // 110px spacing between ingredients

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

            // Animate left group
            for (int i = 0; i < leftGroup.size(); i++) {
                IngredientItem ingredient = leftGroup.get(i);
                if (ingredient.getOriginalPosition() == null) {
                    // Skip if no original position (shouldn't happen, but just in case)
                    continue;
                }
                Point originalPos = ingredient.getOriginalPosition();
                int targetX = leftGroupStartX + (i * columnWidth);
                int targetY = groupsY;

                // Calculate current position based on progress
                int currentX = originalPos.x + (int)((targetX - originalPos.x) * easedProgress);
                int currentY = originalPos.y + (int)((targetY - originalPos.y) * easedProgress);

                ingredient.setLocation(currentX, currentY);
                
                // Ensure ingredient is visible by bringing it to front
                gridPanel.setComponentZOrder(ingredient, 0);
            }

            // Animate right group
            for (int i = 0; i < rightGroup.size(); i++) {
                IngredientItem ingredient = rightGroup.get(i);
                if (ingredient.getOriginalPosition() == null) {
                    // Skip if no original position
                    continue;
                }
                Point originalPos = ingredient.getOriginalPosition();
                int targetX = rightGroupStartX + (i * columnWidth);
                int targetY = groupsY;

                // Calculate current position based on progress
                int currentX = originalPos.x + (int)((targetX - originalPos.x) * easedProgress);
                int currentY = originalPos.y + (int)((targetY - originalPos.y) * easedProgress);

                ingredient.setLocation(currentX, currentY);
                
                // Ensure ingredient is visible by bringing it to front
                gridPanel.setComponentZOrder(ingredient, 0);
            }

            // If animation is complete
            if (currentFrame[0] >= totalFrames) {
                ((Timer)e.getSource()).stop();

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
    * Display potion options in Phase 3
    */
    private void displayPotionOptions() {
        // Clear the grid panel
        gridPanel.removeAll();

        // Create potion option labels
        IngredientItem frostPotion = new IngredientItem(1, "blue");
        frostPotion.setPotionType("Fire Resistance Potion");
        frostPotion.setGroupLabel(true);
        frostPotion.setBoxVisible(false); // IMPORTANT: Disable grid box in Phase 3
        frostPotion.setLocation(GameConstants.WINDOW_WIDTH / 4 - INGREDIENT_SIZE, 250);
        frostPotion.setSize(INGREDIENT_SIZE * 2, INGREDIENT_SIZE * 2);
        gridPanel.add(frostPotion);

        IngredientItem strengthPotion = new IngredientItem(2, "red");
        strengthPotion.setPotionType("Strength Potion");
        strengthPotion.setGroupLabel(true);
        strengthPotion.setBoxVisible(false); // IMPORTANT: Disable grid box in Phase 3
        strengthPotion.setLocation((GameConstants.WINDOW_WIDTH * 3) / 4 - INGREDIENT_SIZE, 250);
        strengthPotion.setSize(INGREDIENT_SIZE * 2, INGREDIENT_SIZE * 2);
        gridPanel.add(strengthPotion);

        // Load and display potion images with labels
        ImageIcon frostPotionImg = resourceManager.getImage("/gameproject/resources/potions/fire_resistance_potion.png");
        ImageIcon strengthPotionImg = resourceManager.getImage("/gameproject/resources/potions/strength_potion.png");

        if (frostPotionImg != null) {
            JLabel frostImgLabel = new JLabel(new ImageIcon(frostPotionImg.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
            frostImgLabel.setBounds(GameConstants.WINDOW_WIDTH / 4 - 60, 180, 120, 120);
            gridPanel.add(frostImgLabel);
        }

        if (strengthPotionImg != null) {
            JLabel strengthImgLabel = new JLabel(new ImageIcon(strengthPotionImg.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
            strengthImgLabel.setBounds((GameConstants.WINDOW_WIDTH * 3) / 4 - 60, 180, 120, 120);
            gridPanel.add(strengthImgLabel);
        }

        // Add descriptions with better styling
        JLabel frostLabel = new JLabel("Fire Resistance Potion", JLabel.CENTER);
        frostLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        frostLabel.setForeground(Color.CYAN);
        frostLabel.setBounds(GameConstants.WINDOW_WIDTH / 4 - 150, 400, 300, 30);
        gridPanel.add(frostLabel);

        JLabel strengthLabel = new JLabel("Strength Potion", JLabel.CENTER);
        strengthLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        strengthLabel.setForeground(Color.RED);
        strengthLabel.setBounds((GameConstants.WINDOW_WIDTH * 3) / 4 - 150, 400, 300, 30);
        gridPanel.add(strengthLabel);

        // Add potion descriptions
        JTextArea frostDesc = new JTextArea("Protects against fire attacks and extreme heat.");
        frostDesc.setEditable(false);
        frostDesc.setWrapStyleWord(true);
        frostDesc.setLineWrap(true);
        frostDesc.setOpaque(false);
        frostDesc.setForeground(Color.WHITE);
        frostDesc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        frostDesc.setBounds(GameConstants.WINDOW_WIDTH / 4 - 150, 430, 300, 60);
        gridPanel.add(frostDesc);

        JTextArea strengthDesc = new JTextArea("Enhances physical strength and combat abilities.");
        strengthDesc.setEditable(false);
        strengthDesc.setWrapStyleWord(true);
        strengthDesc.setLineWrap(true);
        strengthDesc.setOpaque(false);
        strengthDesc.setForeground(Color.WHITE);
        strengthDesc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        strengthDesc.setBounds((GameConstants.WINDOW_WIDTH * 3) / 4 - 150, 430, 300, 60);
        gridPanel.add(strengthDesc);

        // Update instruction
        instructionLabel.setText("Use your 'Mind of Unity' to choose which potion to craft. Click on a potion to select it.");

        // Disable check button until a potion is selected
        checkButton.setEnabled(false);

        // Update UI
        gridPanel.revalidate();
        gridPanel.repaint();
    }
    
    /**
    * Check if the current phase is completed
    */
    private void checkPhaseCompletion() {
        if (currentPhase == 1) {
            // Check if exactly 10 ingredients are selected
            if (selectedIngredients.size() == MAX_SELECTIONS) {
                // Check if selected ingredients form valid runs
                boolean hasValidRuns = checkValidRuns();

                if (hasValidRuns) {
                    JOptionPane.showMessageDialog(this,
                        "Well done! Your Eye of Pattern has identified natural sequences!",
                        "Phase 1 Complete",
                        JOptionPane.INFORMATION_MESSAGE
                    );

                    // Mark phase as completed
                    phaseCompleted = true;

                    // Move to next phase after delay
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

                    // Lose a life for incorrect solution
                    loseLife();
                }
            }
        } else if (currentPhase == 2) {
            // Check if both groups are properly sorted
            isLeftGroupSorted = isGroupSorted(leftGroup);
            isRightGroupSorted = isGroupSorted(rightGroup);

            if (isLeftGroupSorted && isRightGroupSorted) {
                JOptionPane.showMessageDialog(this,
                    "Well done! Your Hand of Balance has arranged the ingredients perfectly!",
                    "Phase 2 Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Mark phase as completed
                phaseCompleted = true;

                // Move to next phase after delay
                Timer transitionTimer = new Timer(1500, e -> {
                    advanceToNextPhase();
                });
                transitionTimer.setRepeats(false);
                transitionTimer.start();
            } else {
                JOptionPane.showMessageDialog(this,
                    "The ingredients aren't properly sorted yet. Try again!",
                    "Try Again",
                    JOptionPane.WARNING_MESSAGE
                );

                // Lose a life for incorrect solution
                loseLife();
            }
        } else if (currentPhase == 3) {
            // Check if a potion has been selected
            if (craftedPotion != null) {
                // Start the boss battle visualization first
                startBossBattle("Flameclaw");

                // Result handling is now in the startBossBattle method
                // No need for additional code here
            }
        }
    }
    
    
    
    
    /**
    * Display boss battle and determine the outcome after animation
    */
    private void startBossBattle(String bossName) {
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

            // Determine the battle outcome
            boolean correctChoice = "Fire Resistance Potion".equals(craftedPotion);

            if (correctChoice) {
                JOptionPane.showMessageDialog(this,
                    "Excellent choice! The Fire Resistance Potion protected you from Flameclaw's attacks!",
                    "Success!",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Signal successful boss battle
                controller.onBossBattleComplete(true, 1);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Oh no! The Strength Potion wasn't effective against Flameclaw's flames!",
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

            // IMPORTANT: Use arrangeIngredientsForSorting instead of useAbility
            arrangeIngredientsForSorting();
        } else if (currentPhase == 3) {
            phaseLabel.setText("Phase 3: The Mind of Unity");
            abilityButton.setText("Use Mind of Unity");
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
                instructionLabel.setText("Use your 'Mind of Unity' to choose which potion to craft.");
                // Setup Phase 3
                displayPotionOptions();
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
     * selected, highlighted, and arranged
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

            setOpaque(false);
            setSize(INGREDIENT_SIZE, INGREDIENT_SIZE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Load grid box image
            gridBoxImage = resourceManager.getImage("/gameproject/resources/grid_box.png");

            // Set grid boxes always visible
            this.isBoxVisible = true;
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
        
         
        
        // New method to load ingredient image using your actual filenames
        private void loadIngredientImage() {
            if (ingredientName == null) return;

            String imagePath = "/gameproject/resources/ingredients/" + ingredientName + ".png";
            ingredientImage = resourceManager.getImage(imagePath);
        }
    
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

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

            // Draw highlight if applicable
            if (isHighlighted) {
                g2d.setColor(highlightColor);
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);
            }

            // Draw selection indicator
            if (isSelected) {
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);
            }

            // Draw the ingredient image
            if (ingredientImage != null) {
                // Calculate size to maintain aspect ratio
                Image img = ingredientImage.getImage();
                int imgWidth = img.getWidth(this);
                int imgHeight = img.getHeight(this);

                // Skip if image hasn't loaded
                if (imgWidth <= 0 || imgHeight <= 0) return;

                // Scale image to fit within panel with margins
                double scale = Math.min(
                    (getWidth() - 20) / (double)imgWidth,
                    (getHeight() - 20) / (double)imgHeight
                );

                int scaledWidth = (int)(imgWidth * scale);
                int scaledHeight = (int)(imgHeight * scale);

                // Center the image
                int x = (getWidth() - scaledWidth) / 2;
                int y = (getHeight() - scaledHeight) / 2;

                // Draw the image
                g2d.drawImage(img, x, y, scaledWidth, scaledHeight, this);

                // Draw value indicator
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillOval(getWidth() - 20, getHeight() - 20, 16, 16);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 11));

                String valueStr = String.valueOf(value);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = getWidth() - 20 + (16 - fm.stringWidth(valueStr)) / 2;
                int textY = getHeight() - 20 + ((16 - fm.getHeight()) / 2) + fm.getAscent();

                g2d.drawString(valueStr, textX, textY);
            }
        }

        
        
        
        // New method to draw ingredient image with proper sizing
        private void drawIngredientImage(Graphics2D g2d) {
            // Calculate the size to maintain aspect ratio
            Image img = ingredientImage.getImage();
            int imgWidth = img.getWidth(this);
            int imgHeight = img.getHeight(this);

            // Skip if image hasn't loaded yet
            if (imgWidth <= 0 || imgHeight <= 0) return;

            // Scale to fit within the panel with some margin
            double scale = Math.min(
                (getWidth() - 20) / (double)imgWidth,
                (getHeight() - 20) / (double)imgHeight
            );

            int scaledWidth = (int)(imgWidth * scale);
            int scaledHeight = (int)(imgHeight * scale);

            // Center the image
            int x = (getWidth() - scaledWidth) / 2;
            int y = (getHeight() - scaledHeight) / 2;

            // Draw the image
            g2d.drawImage(img, x, y, scaledWidth, scaledHeight, this);

            // Draw value in the corner for debugging or reference
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillOval(getWidth() - 20, getHeight() - 20, 16, 16);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 11));

            String valueStr = String.valueOf(value);
            FontMetrics fm = g2d.getFontMetrics();
            int textX = getWidth() - 20 + (16 - fm.stringWidth(valueStr)) / 2;
            int textY = getHeight() - 20 + ((16 - fm.getHeight()) / 2) + fm.getAscent();

            g2d.drawString(valueStr, textX, textY);
        }
        

        /**
         * Draw a regular ingredient
         */
        private void drawIngredient(Graphics2D g2d) {
            // Original drawing code for fallback
            // Map color string to actual color
            Color actualColor;
            switch (color.toLowerCase()) {
                case "blue": actualColor = new Color(100, 150, 255); break;
                case "red": actualColor = new Color(255, 100, 100); break;
                case "green": actualColor = new Color(100, 255, 100); break;
                case "yellow": actualColor = new Color(255, 255, 100); break;
                case "purple": actualColor = new Color(200, 100, 255); break;
                default: actualColor = Color.GRAY;
            }

            // Draw ingredient shape (circle)
            g2d.setColor(actualColor);
            g2d.fillOval(8, 8, getWidth() - 16, getHeight() - 16);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(8, 8, getWidth() - 16, getHeight() - 16);

            // Draw value
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));

            String valueStr = String.valueOf(value);
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(valueStr)) / 2;
            int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            g2d.drawString(valueStr, textX, textY);
        }
        
        /**
         * Draw a potion bottle
         */
        private void drawPotion(Graphics2D g2d) {
            // Draw bottle shape
            int bottleNeckWidth = getWidth() / 4;
            int bottleNeckHeight = getHeight() / 4;
            int bottleBodyWidth = getWidth() - 16;
            int bottleBodyHeight = getHeight() - bottleNeckHeight - 8;
            
            // Determine color based on potion type
            Color potionColor;
            if ("Fire Resistance Potion".equals(potionType)) {
                potionColor = new Color(100, 200, 255); // Ice blue
            } else {
                potionColor = new Color(255, 100, 0); // Fiery orange
            }
            
            // Draw cork
            g2d.setColor(new Color(150, 100, 50));
            g2d.fillRect(getWidth()/2 - bottleNeckWidth/2, 4, bottleNeckWidth, 8);
            
            // Draw bottle neck
            g2d.setColor(new Color(220, 220, 255, 180));
            g2d.fillRect(getWidth()/2 - bottleNeckWidth/2, 12, bottleNeckWidth, bottleNeckHeight);
            
            // Draw bottle body
            g2d.setColor(new Color(220, 220, 255, 180));
            g2d.fillRoundRect(8, bottleNeckHeight + 8, bottleBodyWidth, bottleBodyHeight, 20, 20);
            
            // Draw liquid
            g2d.setColor(potionColor);
            int liquidHeight = (int)(bottleBodyHeight * 0.8);
            g2d.fillRoundRect(12, bottleNeckHeight + 8 + bottleBodyHeight - liquidHeight, 
                              bottleBodyWidth - 8, liquidHeight, 16, 16);
            
            // Draw shine
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillOval(bottleBodyWidth / 2, bottleNeckHeight + 16, 12, 24);
            
            // Draw outline
            g2d.setColor(Color.WHITE);
            g2d.drawRect(getWidth()/2 - bottleNeckWidth/2, 12, bottleNeckWidth, bottleNeckHeight);
            g2d.drawRoundRect(8, bottleNeckHeight + 8, bottleBodyWidth, bottleBodyHeight, 20, 20);
            
            // Draw potion name
            if (potionType != null) {
                g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
                FontMetrics fm = g2d.getFontMetrics();
                
                String shortName = potionType.replace(" Potion", "");
                int textX = (getWidth() - fm.stringWidth(shortName)) / 2;
                int textY = getHeight() - 10;
                
                g2d.setColor(Color.WHITE);
                g2d.drawString(shortName, textX, textY);
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
}