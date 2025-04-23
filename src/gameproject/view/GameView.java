// src/gameproject/view/GameView.java (updated)

package gameproject.view;

import gameproject.controller.GameController;
import gameproject.model.LevelConfig;
import gameproject.model.GameState;
import gameproject.ui.GameGrid;
import gameproject.util.GameConstants;
import gameproject.util.ResourceManager;
import javax.swing.plaf.basic.BasicButtonUI;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Enhanced view for the game screen showing sorting challenges
 */
public class GameView extends JPanel {
    private GameController controller;
    private ResourceManager resourceManager;
    private JPanel heartsPanel;
    private JLabel[] heartLabels;
    private JLabel instructionsLabel;
    private JLabel timerLabel;
    private JButton pauseButton;
    private JButton hintButton;
    private JButton checkButton;
    private JButton nextLevelButton;
    private GameGrid gameGrid;
    
    private LevelConfig currentLevel;
    private Timer gameTimer;
    private int timeRemaining = 300; // 5 minutes in seconds
    private int livesRemaining = 3;
    private boolean levelCompleted = false;
    private ImageIcon backgroundImage;
    private Font pixelifySansFont;
    
    // Button images
    private ImageIcon pauseNormalIcon;
    private ImageIcon pauseHoverIcon;
    private ImageIcon hintNormalIcon;
    private ImageIcon hintHoverIcon;
    private ImageIcon heartFilledIcon;
    private ImageIcon heartEmptyIcon;
    
    /**
     * Constructor - Initialize the enhanced game view
     */
    public GameView(GameController controller) {
        this.controller = controller;
        this.resourceManager = ResourceManager.getInstance();
        
        // Use null layout to position components precisely
        setLayout(null);
        
        // Load resources
        loadResources();
        
        // Create UI components
        createUIComponents();
        
        // Initialize timer to count down from 5 minutes
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTimer();
            }
        });
    }
    
    /**
     * Load all required resources
     */
    private void loadResources() {
        // Load background image
        backgroundImage = resourceManager.getImage("/gameproject/resources/forest_bg.png");
        
        // Load custom font
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
        
        // Load heart images
        heartFilledIcon = resourceManager.getImage("/gameproject/resources/heart_filled.png");
        heartEmptyIcon = resourceManager.getImage("/gameproject/resources/heart_empty.png");
        
        // Scale heart images to 70x70 pixels
        if (heartFilledIcon != null) {
            Image img = heartFilledIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            heartFilledIcon = new ImageIcon(img);
        }
        
        if (heartEmptyIcon != null) {
            Image img = heartEmptyIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            heartEmptyIcon = new ImageIcon(img);
        }
    }
    
    /**
    * Create and position all UI components - update for larger timer
    */
    private void createUIComponents() {
       // Hearts panel for lives - place directly on background with proper spacing
       heartsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
       heartsPanel.setBounds(20, 20, 240, 70);
       heartsPanel.setOpaque(false);
       add(heartsPanel);

       // Initialize hearts with proper spacing
       heartLabels = new JLabel[3];
       for (int i = 0; i < 3; i++) {
           heartLabels[i] = new JLabel(heartFilledIcon);
           heartLabels[i].setOpaque(false);
           heartsPanel.add(heartLabels[i]);
       }

       // Timer display centered at the top without any background and larger font
       timerLabel = new JLabel("05:00", JLabel.CENTER); // Removed "Time: " prefix
       timerLabel.setFont(pixelifySansFont.deriveFont(70f)); // Increased to 70 pixels
       timerLabel.setForeground(Color.WHITE);
       timerLabel.setOpaque(false); // No background
       // Center the timer
       timerLabel.setBounds((GameConstants.WINDOW_WIDTH - 200) / 2, 10, 200, 70);
       add(timerLabel);

       // Custom control buttons - positioned at top right with adequate spacing
       pauseButton = createImageButton(pauseNormalIcon, pauseHoverIcon);
       pauseButton.setBounds(GameConstants.WINDOW_WIDTH - 90, 20, 70, 70); // Now second (right position)
       pauseButton.addActionListener(e -> showPauseMenu());
       add(pauseButton);

       hintButton = createImageButton(hintNormalIcon, hintHoverIcon);
       hintButton.setBounds(GameConstants.WINDOW_WIDTH - 180, 20, 70, 70); // Now first (left position)
       hintButton.addActionListener(e -> controller.showHint());
       add(hintButton);

       // Instructions with improved visibility
       instructionsLabel = new JLabel("Sort these items by value using Insertion Sort. Move the smaller items to the left.", JLabel.CENTER);
       instructionsLabel.setFont(new Font("Arial", Font.BOLD, 16));
       instructionsLabel.setForeground(Color.BLACK);
       instructionsLabel.setBackground(new Color(255, 255, 255, 180));
       instructionsLabel.setOpaque(true);
       instructionsLabel.setBounds(0, 100, GameConstants.WINDOW_WIDTH, 30);
       add(instructionsLabel);

       // Bottom buttons with proper spacing - no background panel
       checkButton = new JButton("Check Solution");
       checkButton.setBounds((GameConstants.WINDOW_WIDTH / 2) - 180, GameConstants.WINDOW_HEIGHT - 60, 150, 30);
       styleButton(checkButton);
       checkButton.addActionListener(e -> checkSolution());
       add(checkButton);

       nextLevelButton = new JButton("Next Level");
       nextLevelButton.setBounds((GameConstants.WINDOW_WIDTH / 2) + 30, GameConstants.WINDOW_HEIGHT - 60, 150, 30);
       styleButton(nextLevelButton);
       nextLevelButton.setEnabled(false);
       nextLevelButton.addActionListener(e -> controller.goToNextLevel());
       add(nextLevelButton);
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
    * Display pause menu overlay - with semi-transparent dark background
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

       // Create menu container panel with same layout as before
       JPanel menuPanel = new JPanel();
       menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
       menuPanel.setOpaque(false); // Transparent panel
       menuPanel.setBorder(null); // No border
       menuPanel.setBounds(GameConstants.WINDOW_WIDTH / 2 - 150, GameConstants.WINDOW_HEIGHT / 2 - 150, 300, 300);

       // Use AnimatedButton from MainMenuView
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
           resetLevel();
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
               g.setColor(new Color(0, 0, 0, 153)); // 153 is ~60% opacity
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
           resetLevel();
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
     * Create a pixelated style button with PixelifySans font
     */
    private JButton createPixelButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(pixelifySansFont);
        button.setForeground(Color.WHITE);
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.setFocusPainted(false);
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(80, 80, 80));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.BLACK);
            }
        });
        
        return button;
    }
    
    /**
     * Reset the current level completely
     */
    private void resetLevel() {
        // Reset lives
        resetLives();
        
        // Reset timer
        timeRemaining = 300; // 5 minutes
        updateTimerDisplay();
        
        // Restart the level
        controller.restartLevel();
    }
    
    /**
     * Update the level information display
     */
    public void updateLevelInfo(String difficulty, int level) {
        // Reset lives to full
        resetLives();
        
        // Reset timer to 5 minutes
        timeRemaining = 300;
        updateTimerDisplay();
        
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
            
            // Start timer automatically
            gameTimer.start();
        }
    }
    
    /**
    * Initialize the level with the game grid - with smaller grid
    */
    private void initializeLevel() {
       // Remove existing grid if any
       if (gameGrid != null) {
           remove(gameGrid);
       }

       // Reset level state
       levelCompleted = false;
       nextLevelButton.setEnabled(false);

       // Load appropriate background based on level theme
       String bgPath = "/gameproject/resources/" + currentLevel.getBackgroundTheme().toLowerCase() + "_bg.png";
       backgroundImage = resourceManager.getImage(bgPath);
       if (backgroundImage == null) {
           backgroundImage = resourceManager.getImage("/gameproject/resources/default_bg.png");
       }

       // Create new game grid with smaller size
       gameGrid = new GameGrid(currentLevel);
       // Make grid smaller by reducing width and height
       gameGrid.setBounds((GameConstants.WINDOW_WIDTH - 700) / 2, 150, 700, 450); // Reduced from 800x520
       add(gameGrid);

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
     * Apply consistent styling to buttons
     */
    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        // Remove any internal padding that might create the white space
        button.setMargin(new Insets(0, 0, 0, 0));
    }
    
    /**
    * Check if the current solution is correct
    */
    private void checkSolution() {
        if (levelCompleted) return;

        // Get the current state from the grid
        int[] currentArray = gameGrid.getCurrentState();

        // Check if solution is correct
        boolean isCorrect = currentLevel.validateSolution(currentArray);

        if (isCorrect) {
            // Stop timer
            gameTimer.stop();

            // Calculate stars based on time and steps
            int stars = calculateStars();

            // Mark level as completed
            levelCompleted = true;
            nextLevelButton.setEnabled(true);

            // Show success message
            JOptionPane.showMessageDialog(this,
                    "Congratulations! You've completed this level with " + stars + " stars!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // Record progress
            controller.completeLevelWithStars(currentLevel.getDifficulty(), 
                    currentLevel.getLevelNumber(), stars);
        } else {
            // Lose a life
            loseLife();

            // Only show error message if not showing level failed screen
            if (livesRemaining > 0) {
                JOptionPane.showMessageDialog(this,
                        "Not quite right yet. Keep trying!\nLives remaining: " + livesRemaining,
                        "Try Again", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    /**
     * Calculate stars based on performance
     */
    private int calculateStars() {
        // Base stars - completing gives at least 1 star
        int stars = 1;
        
        // Add stars based on lives remaining
        stars += livesRemaining;
        
        // Cap at 3 stars
        return Math.min(stars, 3);
    }
    
    /**
     * Update the timer - now counting down from 5 minutes
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
    * Update the timer display - no "Time: " prefix
    */
    private void updateTimerDisplay() {
       int minutes = timeRemaining / 60;
       int seconds = timeRemaining % 60;
       timerLabel.setText(String.format("%02d:%02d", minutes, seconds)); // Removed "Time: " prefix
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw the background image to fill the entire panel
        if (backgroundImage != null) {
            g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback to solid color if image is not available
            g.setColor(new Color(240, 240, 240));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}