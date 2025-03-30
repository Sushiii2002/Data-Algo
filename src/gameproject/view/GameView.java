package gameproject.view;

import gameproject.controller.GameController;
import gameproject.model.LevelConfig;
import gameproject.service.SortingService;
import gameproject.service.SortingService.SortStep;
import gameproject.ui.DraggableItem;
import gameproject.ui.DraggableItem.DragListener;
import gameproject.util.GameConstants;
import gameproject.util.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced view for the game screen showing sorting challenges
 */
public class GameView extends JPanel {
    private GameController controller;
    private ResourceManager resourceManager;
    private JLabel levelLabel;
    private JPanel sortingArea;
    private JLabel instructionsLabel;
    private JLabel timerLabel;
    private JButton hintButton;
    private JButton checkButton;
    private JButton nextButton;
    private JButton restartButton;
    private JButton menuButton;
    
    private List<DraggableItem> draggableItems;
    private LevelConfig currentLevel;
    private SortingService sortingService;
    private Timer gameTimer;
    private int timeRemaining;
    private boolean levelCompleted = false;
    
    /**
     * Constructor - Initialize the enhanced game view
     */
    public GameView(GameController controller) {
        this.controller = controller;
        this.resourceManager = ResourceManager.getInstance();
        this.sortingService = new SortingService();
        this.draggableItems = new ArrayList<>();
        
        setLayout(new BorderLayout());
        
        // Top panel with level info
        createTopPanel();
        
        // Main game area
        createGameArea();
        
        // Bottom panel with action buttons
        createBottomPanel();
        
        // Initialize timer
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTimer();
            }
        });
    }
    
    /**
     * Create the top panel with level info and controls
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // Level info
        levelLabel = new JLabel("Level 1 - Beginner");
        levelLabel.setFont(resourceManager.getFont(GameConstants.FONT_PATH, 18f));
        levelLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));
        
        // Timer label
        timerLabel = new JLabel("Time: --:--");
        timerLabel.setFont(resourceManager.getFont(GameConstants.FONT_PATH, 16f));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 20));
        
        // Control buttons
        JPanel controlPanel = new JPanel();
        hintButton = new JButton("Hint");
        restartButton = new JButton("Restart");
        menuButton = new JButton("Main Menu");
        
        hintButton.addActionListener(e -> controller.showHint());
        restartButton.addActionListener(e -> controller.restartLevel());
        menuButton.addActionListener(e -> controller.showMainMenu());
        
        // Style buttons
        styleButton(hintButton);
        styleButton(restartButton);
        styleButton(menuButton);
        
        controlPanel.add(hintButton);
        controlPanel.add(restartButton);
        controlPanel.add(menuButton);
        
        // Assemble top panel
        JPanel levelInfoPanel = new JPanel(new BorderLayout());
        levelInfoPanel.add(levelLabel, BorderLayout.WEST);
        levelInfoPanel.add(timerLabel, BorderLayout.EAST);
        
        topPanel.add(levelInfoPanel, BorderLayout.WEST);
        topPanel.add(controlPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
    }
    
    /**
     * Create the main game area
     */
    private void createGameArea() {
        JPanel gameArea = new JPanel();
        gameArea.setLayout(new BoxLayout(gameArea, BoxLayout.Y_AXIS));
        gameArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Instructions
        instructionsLabel = new JLabel("Drag and drop the elements to sort them:");
        instructionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        instructionsLabel.setFont(resourceManager.getFont(GameConstants.FONT_PATH, 16f));
        
        // Sorting visualization area
        sortingArea = new JPanel(null); // Using null layout for precise positioning
        sortingArea.setBackground(Color.LIGHT_GRAY);
        sortingArea.setPreferredSize(new Dimension(800, 300));
        sortingArea.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        sortingArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add components to game area
        gameArea.add(instructionsLabel);
        gameArea.add(Box.createRigidArea(new Dimension(0, 20)));
        gameArea.add(sortingArea);
        
        add(gameArea, BorderLayout.CENTER);
    }
    
    /**
     * Create the bottom panel with action buttons
     */
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        checkButton = new JButton("Check Solution");
        nextButton = new JButton("Next Step");
        
        checkButton.addActionListener(e -> checkSolution());
        nextButton.addActionListener(e -> nextStep());
        
        styleButton(checkButton);
        styleButton(nextButton);
        
        bottomPanel.add(checkButton);
        bottomPanel.add(nextButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Apply consistent styling to buttons
     */
    private void styleButton(JButton button) {
        button.setFont(resourceManager.getFont(GameConstants.FONT_PATH, 14f));
        button.setFocusPainted(false);
    }
    
    /**
     * Update the level information display
     */
    public void updateLevelInfo(String difficulty, int level) {
        levelLabel.setText("Level " + level + " - " + difficulty);
        
        // Get the level configuration
        List<LevelConfig> allLevels = LevelConfig.createAllLevels();
        for (LevelConfig config : allLevels) {
            if (config.getDifficulty().equals(difficulty) && config.getLevelNumber() == level) {
                currentLevel = config;
                break;
            }
        }
        
        if (currentLevel != null) {
            // Update instructions
            instructionsLabel.setText(currentLevel.getInstruction());
            
            // Initialize the level
            initializeLevel();
        }
    }
    
    /**
     * Initialize the level with draggable items
     */
    private void initializeLevel() {
        // Clear existing items
        sortingArea.removeAll();
        draggableItems.clear();
        levelCompleted = false;
        
        // Get initial array
        int[] initialArray = currentLevel.getInitialArray();
        
        // Create draggable items
        int startX = 50;
        int startY = 50;
        int spacing = 60;
        
        for (int i = 0; i < initialArray.length; i++) {
            int value = initialArray[i];
            // Generate a color based on the value
            Color itemColor = new Color(
                    50 + (value * 10) % 150,
                    80 + (value * 7) % 120,
                    100 + (value * 13) % 155
            );
            
            DraggableItem item = new DraggableItem(value, itemColor);
            
            // Position the item
            int x = startX + (i * spacing);
            item.setLocation(x, startY);
            item.setOriginalPosition(new Point(x, startY));
            
            // Set drag listener
            item.setDragListener(new DragListener() {
                @Override
                public void onDragStart(DraggableItem item) {
                    // Highlight if needed
                }
                
                @Override
                public void onDragEnd(DraggableItem item) {
                    // Check for swap or reposition
                    handleItemDrop(item);
                }
                
                @Override
                public void onDragging(DraggableItem item, Point currentPos) {
                    // Real-time feedback if needed
                }
            });
            
            draggableItems.add(item);
            sortingArea.add(item);
        }
        
        // Set up timer if there's a time limit
        if (currentLevel.getTimeLimit() > 0) {
            timeRemaining = currentLevel.getTimeLimit();
            updateTimerDisplay();
            gameTimer.start();
        } else {
            timerLabel.setText("Time: --:--");
        }
        
        sortingArea.revalidate();
        sortingArea.repaint();
    }
    
    /**
     * Handle dropping a draggable item
     */
    private void handleItemDrop(DraggableItem droppedItem) {
        if (levelCompleted) return;
        
        // Find the closest position
        int closestIndex = -1;
        int minDistance = Integer.MAX_VALUE;
        
        for (int i = 0; i < draggableItems.size(); i++) {
            DraggableItem item = draggableItems.get(i);
            int distance = (int) droppedItem.getLocation().distance(item.getOriginalPosition());
            
            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }
        
        if (closestIndex != -1) {
            // Get the current index of the dropped item
            int currentIndex = draggableItems.indexOf(droppedItem);
            
            // Swap positions if needed
            if (currentIndex != closestIndex) {
                // Save the target position
                Point targetPos = draggableItems.get(closestIndex).getOriginalPosition();
                
                // Update the positions of all items in between
                if (currentIndex < closestIndex) {
                    // Shift items left
                    for (int i = currentIndex + 1; i <= closestIndex; i++) {
                        DraggableItem item = draggableItems.get(i);
                        Point prevPos = draggableItems.get(i - 1).getOriginalPosition();
                        item.setLocation(prevPos);
                        item.setOriginalPosition(prevPos);
                    }
                } else {
                    // Shift items right
                    for (int i = currentIndex - 1; i >= closestIndex; i--) {
                        DraggableItem item = draggableItems.get(i);
                        Point nextPos = draggableItems.get(i + 1).getOriginalPosition();
                        item.setLocation(nextPos);
                        item.setOriginalPosition(nextPos);
                    }
                }
                
                // Move dropped item to target position
                droppedItem.setLocation(targetPos);
                droppedItem.setOriginalPosition(targetPos);
                
                // Update the list order
                DraggableItem temp = draggableItems.remove(currentIndex);
                draggableItems.add(closestIndex, temp);
                
                sortingArea.repaint();
            } else {
                // Just snap back to original position
                droppedItem.setLocation(droppedItem.getOriginalPosition());
            }
        }
    }
    
    /**
     * Check if the current solution is correct
     */
    private void checkSolution() {
        if (levelCompleted) return;
        
        // Extract values from draggable items
        int[] currentArray = new int[draggableItems.size()];
        for (int i = 0; i < draggableItems.size(); i++) {
            currentArray[i] = draggableItems.get(i).getValue();
        }
        
        // Check if solution is correct
        boolean isCorrect = currentLevel.validateSolution(currentArray);
        
        if (isCorrect) {
            // Stop timer
            if (gameTimer.isRunning()) {
                gameTimer.stop();
            }
            
            // Calculate stars based on time and steps
            int stars = calculateStars();
            
            // Mark level as completed
            levelCompleted = true;
            
            // Show success message
            JOptionPane.showMessageDialog(this,
                    "Congratulations! You've completed this level with " + stars + " stars!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Record progress
            controller.completeLevelWithStars(currentLevel.getDifficulty(), 
                    currentLevel.getLevelNumber(), stars);
            
            // Enable next button
            nextButton.setEnabled(true);
        } else {
            // Show error message
            JOptionPane.showMessageDialog(this,
                    "Not quite right yet. Keep trying!",
                    "Try Again", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Calculate stars based on performance
     */
    private int calculateStars() {
        // Base stars - completing gives at least 1 star
        int stars = 1;
        
        // Check if we have time remaining (if there was a time limit)
        if (currentLevel.getTimeLimit() > 0) {
            // Time bonus - more than 50% time remaining gives extra star
            float timePercentRemaining = (float) timeRemaining / currentLevel.getTimeLimit();
            if (timePercentRemaining > 0.5) {
                stars++;
            }
        }
        
        // TODO: Add step count tracking for the third star
        // For now, just give 3 stars for all completed levels
        stars = 3;
        
        return stars;
    }
    
    /**
     * Move to next level
     */
    private void nextStep() {
        if (levelCompleted) {
            controller.goToNextLevel();
        } else {
            // Just advance the current demonstration
            JOptionPane.showMessageDialog(this,
                    "Complete the current level first!",
                    "Complete Level", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Update the timer
     */
    private void updateTimer() {
        if (timeRemaining > 0) {
            timeRemaining--;
            updateTimerDisplay();
            
            if (timeRemaining <= 0) {
                // Time's up
                gameTimer.stop();
                JOptionPane.showMessageDialog(this,
                        "Time's up! Try again.",
                        "Time Expired", JOptionPane.INFORMATION_MESSAGE);
                
                // Restart level
                controller.restartLevel();
            }
        }
    }
    
    /**
     * Update the timer display
     */
    private void updateTimerDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }
}