package gameproject.ui;

import gameproject.controller.GameController;
import gameproject.model.GameState;
import gameproject.util.ResourceManager;
import gameproject.util.GameConstants;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the visualization and interaction for the TimSort algorithm phases
 * in the RPG game, with UI controls from the original game.
 * Timer and hearts have been removed as requested.
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
    
    // UI controls
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
    private static final int GRID_COLS = 8;
    private static final int MAX_SELECTIONS = 10;
    private static final int INGREDIENT_SIZE = 64;
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
        
        // Add keyboard shortcut for pause (ESC key)
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "showPause");
        actionMap.put("showPause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPauseMenu();
            }
        });
        
        // Add keyboard shortcut for hint (H key)
        inputMap.put(KeyStroke.getKeyStroke("H"), "showHint");
        actionMap.put("showHint", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHint();
            }
        });
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
     * Timer, hearts, and top-right buttons have been removed
     */
    private void initializeUI() {
        // Top-right buttons (pause and hint) have been removed
        
        // Main heading/phase display
        phaseLabel = new JLabel("Phase 1: The Eye of Pattern", JLabel.CENTER);
        phaseLabel.setFont(pixelifySansFont.deriveFont(30f)); // Increased size since we have more space
        phaseLabel.setForeground(Color.WHITE);
        phaseLabel.setBounds(0, 40, GameConstants.WINDOW_WIDTH, 40); // Moved up since we have more space
        add(phaseLabel);
        
        // Instructions
        instructionLabel = new JLabel(
            "Use your 'Eye of Pattern' ability to identify ingredient sequences (runs).", 
            JLabel.CENTER
        );
        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 18)); // Increased font size
        instructionLabel.setForeground(Color.WHITE);
        instructionLabel.setBounds(0, 90, GameConstants.WINDOW_WIDTH, 30);
        add(instructionLabel);
        
        // Grid panel for ingredients
        gridPanel = new JPanel(null); // Use null layout for precise positioning
        gridPanel.setBounds(
            (GameConstants.WINDOW_WIDTH - (GRID_COLS * INGREDIENT_SIZE)) / 2, 
            140, // Moved up since we removed timer and hearts
            GRID_COLS * INGREDIENT_SIZE, 
            GRID_ROWS * INGREDIENT_SIZE
        );
        gridPanel.setOpaque(false);
        add(gridPanel);
        
        // Control panel
        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBounds(0, GameConstants.WINDOW_HEIGHT - 100, GameConstants.WINDOW_WIDTH, 80);
        controlPanel.setOpaque(false);
        
        // Ability button
        abilityButton = createStyledButton("Use Eye of Pattern");
        abilityButton.addActionListener(e -> useAbility());
        abilityButton.setPreferredSize(new Dimension(180, 40)); // Wider button
        abilityButton.setFont(new Font("SansSerif", Font.BOLD, 16)); // Larger font
        controlPanel.add(abilityButton);
        
        // Check button
        checkButton = createStyledButton("Check Selection");
        checkButton.addActionListener(e -> checkPhaseCompletion());
        checkButton.setEnabled(false);
        checkButton.setPreferredSize(new Dimension(180, 40)); // Wider button
        checkButton.setFont(new Font("SansSerif", Font.BOLD, 16)); // Larger font
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
     * Generate ingredients for the grid
     */
    private void generateIngredients() {
        allIngredients.clear();
        selectedIngredients.clear();
        
        // Clear the grid panel
        gridPanel.removeAll();
        
        // Create three sequences of ingredients with natural runs
        // Run 1: Blue ingredients (values 1-4)
        addIngredientsRun(1, 4, "blue");
        
        // Run 2: Red ingredients (values 5-8)
        addIngredientsRun(5, 8, "red");
        
        // Run 3: Green ingredients (values 9-12)
        addIngredientsRun(9, 12, "green");
        
        // Add some random ingredients to fill the grid
        addRandomIngredients(40 - allIngredients.size());
        
        // Randomly position ingredients in the grid
        positionIngredientsRandomly();
        
        // Repaint
        gridPanel.revalidate();
        gridPanel.repaint();
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
                250
            );
        }
        
        // Update right group display
        for (int i = 0; i < rightGroup.size(); i++) {
            IngredientItem ingredient = rightGroup.get(i);
            
            // Position in right group
            ingredient.setLocation(
                450 + (i * INGREDIENT_SIZE),
                250
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
            // Hand of Balance - Arrange ingredients
            arrangeIngredientsForSorting();
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
        
        // Find runs of blue ingredients
        List<IngredientItem> blueRun = findRunByColor("blue");
        if (blueRun.size() >= 3) {
            identifiedRuns.add(blueRun);
            highlightRun(blueRun, new Color(0, 0, 255, 80));
        }
        
        // Find runs of red ingredients
        List<IngredientItem> redRun = findRunByColor("red");
        if (redRun.size() >= 3) {
            identifiedRuns.add(redRun);
            highlightRun(redRun, new Color(255, 0, 0, 80));
        }
        
        // Find runs of green ingredients
        List<IngredientItem> greenRun = findRunByColor("green");
        if (greenRun.size() >= 3) {
            identifiedRuns.add(greenRun);
            highlightRun(greenRun, new Color(0, 255, 0, 80));
        }
        
        // Update instruction
        instructionLabel.setText("Natural runs highlighted! Select exactly 10 ingredients that form sequences.");
        
        // Enable check button if exactly 10 ingredients are selected
        checkButton.setEnabled(selectedIngredients.size() == MAX_SELECTIONS);
    }
    
    /**
     * Find a run of ingredients by color
     */
    private List<IngredientItem> findRunByColor(String color) {
        List<IngredientItem> run = new ArrayList<>();
        
        // Get all ingredients of this color
        List<IngredientItem> colorIngredients = new ArrayList<>();
        for (IngredientItem ingredient : allIngredients) {
            if (ingredient.getColor().equals(color)) {
                colorIngredients.add(ingredient);
            }
        }
        
        // Sort by value
        colorIngredients.sort((a, b) -> Integer.compare(a.getValue(), b.getValue()));
        
        // Return sorted run
        return colorIngredients;
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
        
        // Update UI
        gridPanel.removeAll();
        
        // Create group headers
        JLabel leftHeader = new JLabel("Frost Ingredients", JLabel.CENTER);
        leftHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        leftHeader.setForeground(Color.CYAN);
        leftHeader.setBounds(50, 200, 300, 30);
        gridPanel.add(leftHeader);
        
        JLabel rightHeader = new JLabel("Power Ingredients", JLabel.CENTER);
        rightHeader.setFont(new Font("SansSerif", Font.BOLD, 16));
        rightHeader.setForeground(Color.RED);
        rightHeader.setBounds(450, 200, 300, 30);
        gridPanel.add(rightHeader);
        
        // Add ingredients that were selected in Phase 1
        for (IngredientItem ingredient : selectedIngredients) {
            ingredient.setSelected(true);
            ingredient.setHighlighted(false);
            
            // Randomly assign to left or right group initially
            if (leftGroup.size() < GROUP_SIZE && Math.random() < 0.5) {
                leftGroup.add(ingredient);
            } else if (rightGroup.size() < GROUP_SIZE) {
                rightGroup.add(ingredient);
            } else {
                leftGroup.add(ingredient);
            }
            
            // Add to grid panel
            gridPanel.add(ingredient);
        }
        
        // Sort and display groups
        sortGroup(leftGroup);
        sortGroup(rightGroup);
        updateGroupDisplay();
        
        // Update instruction
        instructionLabel.setText("Use your 'Hand of Balance' to sort ingredients into two groups. Click on ingredients to move them between groups.");
        
        // Enable check button if both groups are full
        checkButton.setEnabled(leftGroup.size() == GROUP_SIZE && rightGroup.size() == GROUP_SIZE);
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
        frostPotion.setLocation(GameConstants.WINDOW_WIDTH / 4 - INGREDIENT_SIZE, 250);
        frostPotion.setSize(INGREDIENT_SIZE * 2, INGREDIENT_SIZE * 2);
        gridPanel.add(frostPotion);
        
        IngredientItem strengthPotion = new IngredientItem(2, "red");
        strengthPotion.setPotionType("Strength Potion");
        strengthPotion.setGroupLabel(true);
        strengthPotion.setLocation((GameConstants.WINDOW_WIDTH * 3) / 4 - INGREDIENT_SIZE, 250);
        strengthPotion.setSize(INGREDIENT_SIZE * 2, INGREDIENT_SIZE * 2);
        gridPanel.add(strengthPotion);
        
        // Add descriptions
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
                    
                    // No lives to lose now, just allow retrying
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
                
                // No lives to lose now, just allow retrying
            }
        } else if (currentPhase == 3) {
            // Check if a potion has been selected
            if (craftedPotion != null) {
                boolean correctChoice = "Fire Resistance Potion".equals(craftedPotion);
                
                if (correctChoice) {
                    JOptionPane.showMessageDialog(this,
                        "Excellent choice! The Fire Resistance Potion will protect you from Flameclaw's attacks!",
                        "Success!",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // Signal successful boss battle
                    controller.onBossBattleComplete(true, 1);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Oh no! The Strength Potion isn't effective against Flameclaw's flames!",
                        "Incorrect Choice",
                        JOptionPane.WARNING_MESSAGE
                    );
                    
                    // Allow the player to try again since we've removed lives
                    craftedPotion = null;
                    checkButton.setEnabled(false);
                    phaseCompleted = false;
                }
            }
        }
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
        
        // Update phase label
        if (currentPhase == 2) {
            phaseLabel.setText("Phase 2: The Hand of Balance");
            abilityButton.setText("Use Hand of Balance");
            useAbility(); // Set up Phase 2
        } else if (currentPhase == 3) {
            phaseLabel.setText("Phase 3: The Mind of Unity");
            abilityButton.setText("Use Mind of Unity");
            useAbility(); // Set up Phase 3
        } else {
            // Return to story mode after all phases
            controller.returnToStoryMode();
        }
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
        
        // Removed decorative elements that were in top right corner
        // No phase symbols will be drawn now
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
    
    /**
     * Ingredient item class representing a visual ingredient element that can be
     * selected, highlighted, and arranged
     */
    private class IngredientItem extends JPanel {
        private int value;
        private String color;
        private boolean isSelected = false;
        private boolean isHighlighted = false;
        private Color highlightColor = new Color(255, 255, 0, 80);
        private boolean isGroupLabel = false;
        private String potionType = null;
        
        public IngredientItem(int value, String color) {
            this.value = value;
            this.color = color;
            
            setOpaque(false);
            setSize(INGREDIENT_SIZE, INGREDIENT_SIZE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw background for selections and highlights
            if (isHighlighted) {
                g2d.setColor(highlightColor);
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 10, 10);
            }
            
            if (isSelected) {
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g2d.setColor(Color.WHITE);
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
            }
            
            // Draw the ingredient or potion
            if (isGroupLabel) {
                // Draw a potion bottle
                drawPotion(g2d);
            } else {
                // Draw a regular ingredient
                drawIngredient(g2d);
            }
        }
        
        /**
         * Draw a regular ingredient
         */
        private void drawIngredient(Graphics2D g2d) {
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
        
        public void setPotionType(String potionType) {
            this.potionType = potionType;
            repaint();
        }
    }
}