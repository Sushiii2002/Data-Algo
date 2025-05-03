package gameproject.view;

import gameproject.controller.GameController;
import gameproject.util.GameConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * View for the level selection screen with improved centering and star display
 */
public class LevelSelectionView extends JPanel {
    private GameController controller;
    private Font pixelifyFont;
    private ImageIcon backgroundImage;
    private ImageIcon[] levelBoxIcons = new ImageIcon[3];
    private ImageIcon backArrowIcon;
    private ImageIcon filledStarIcon;
    private ImageIcon emptyStarIcon;
    private ImageIcon lockedLevelIcon; // New: level locked icon
    
    // Constants for sizing
    private static final int LEVEL_BOX_SIZE = 190;
    private static final int LEVEL_SPACING = 50;
    private static final int ARROW_SIZE = 80;
    private static final int STAR_SIZE = 52;
    private static final int STAR_SPACING = 5;
    private static final int STARS_TOP_MARGIN = 15;
    
    
    // Store star labels to update them
    private JLabel[][] starLabels = new JLabel[3][3]; // [level][star]
    private JLabel[] lockIcons = new JLabel[3]; // New: store lock overlays
    private BackgroundPanel mainPanel;
    
    /**
     * Constructor - Initialize the level selection view with centered elements
     */
    public LevelSelectionView(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        
        // Load resources
        loadResources();
        
        // Create main panel with background
        mainPanel = new BackgroundPanel();
        mainPanel.setLayout(null); // Use null layout for precise positioning
        
        // Add the main panel to this view
        add(mainPanel);
        
        // Create title label with larger font - precisely centered
        JLabel titleLabel = new JLabel("SELECT A LEVEL", JLabel.CENTER);
        titleLabel.setFont(pixelifyFont.deriveFont(Font.BOLD, 80f));
        titleLabel.setForeground(Color.WHITE);
        // Position title exactly in the center horizontally and at appropriate vertical position
        titleLabel.setBounds(0, 125, GameConstants.WINDOW_WIDTH, 50);
        mainPanel.add(titleLabel);
        
        // Calculate total width of level boxes with spacing
        int totalBoxesWidth = (3 * LEVEL_BOX_SIZE) + (2 * LEVEL_SPACING);
        // Calculate starting X to center the boxes horizontally
        int startX = (GameConstants.WINDOW_WIDTH - totalBoxesWidth) / 2;
        // Vertical position for boxes - centered in the window
        int boxesY = (GameConstants.WINDOW_HEIGHT - LEVEL_BOX_SIZE) / 2 + 50; // Add 50px offset to account for title
        
        // Create exactly 3 level boxes
        for (int i = 0; i < 3; i++) {
            final int level = i + 1;
            
            // Calculate position for this box
            int boxX = startX + (i * (LEVEL_BOX_SIZE + LEVEL_SPACING));
            
            // Add stars above the level box
            // Calculate total width of 3 stars with spacing
            int starsWidth = (3 * STAR_SIZE) + (2 * STAR_SPACING);
            // Calculate starting X to center stars above the level box
            int starStartX = boxX + (LEVEL_BOX_SIZE - starsWidth) / 2;
            // Calculate Y position for stars (above the level box)
            int starsY = boxesY - STAR_SIZE - STARS_TOP_MARGIN;
            
            // Create 3 stars for each level
            for (int j = 0; j < 3; j++) {
                int starX = starStartX + (j * (STAR_SIZE + STAR_SPACING));
                
                // Always start with empty stars
                starLabels[i][j] = new JLabel(emptyStarIcon);
                starLabels[i][j].setBounds(starX, starsY, STAR_SIZE, STAR_SIZE);
                mainPanel.add(starLabels[i][j]);
            }
            
            // Create level box panel
            JPanel levelBox = new JPanel(new BorderLayout());
            levelBox.setOpaque(false);
            levelBox.setBounds(boxX, boxesY, LEVEL_BOX_SIZE, LEVEL_BOX_SIZE);
            
            // Use level box image
            JLabel boxImageLabel = new JLabel(levelBoxIcons[i]);
            boxImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Add box image to level box
            levelBox.add(boxImageLabel, BorderLayout.CENTER);
            
            // Add the lock icon overlay (initially invisible)
            lockIcons[i] = new JLabel(lockedLevelIcon);
            lockIcons[i].setBounds(boxX, boxesY, LEVEL_BOX_SIZE, LEVEL_BOX_SIZE);
            // Only level 1 is unlocked by default
            lockIcons[i].setVisible(i > 0);
            mainPanel.add(lockIcons[i]);
            
            // Make box clickable
            levelBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
            levelBox.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (level == 1) {
                        // Level 1 is always accessible
                        controller.model.setGameLevel(1);
                        controller.startLevel("Beginner", 1);
                    } else if (level == 2) {
                        // Check if Level 1 is completed first
                        if (controller.isLevelCompleted("Beginner", 1)) {
                            controller.model.setGameLevel(2);
                            controller.startLevel("Intermediate", 1);
                        } else {
                            JOptionPane.showMessageDialog(LevelSelectionView.this,
                                "You must complete Level 1 first!",
                                "Level Locked",
                                JOptionPane.WARNING_MESSAGE);
                        }
                    } else if (level == 3) {
                        // Check if Level 2 is completed first
                        if (controller.isLevelCompleted("Intermediate", 1)) {
                            controller.model.setGameLevel(3);
                            controller.startLevel("Advanced", 1);
                        } else {
                            JOptionPane.showMessageDialog(LevelSelectionView.this,
                                "You must complete Level 2 first!",
                                "Level Locked",
                                JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            });
            
            // Add level box to main panel
            mainPanel.add(levelBox);
        }
        
        // Add back button as a smaller image
        JLabel backButton = new JLabel(backArrowIcon);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setBounds(15, 15, ARROW_SIZE, ARROW_SIZE);
        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.showMainMenu();
            }
        });
        mainPanel.add(backButton);
        
        // Update the stars display and lock icons initially
        updateLevelStatus();
    }
    
    /**
     * Load all required resources
     */
    private void loadResources() {
        try {
            // Load custom font
            pixelifyFont = Font.createFont(Font.TRUETYPE_FONT, 
                getClass().getResourceAsStream("/gameproject/resources/PixelifySans.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(pixelifyFont);
            
            // Load images
            backgroundImage = new ImageIcon(getClass().getResource("/gameproject/resources/levelBG.png"));
            
            // Load and resize back arrow image
            ImageIcon originalBackArrow = new ImageIcon(getClass().getResource("/gameproject/resources/arrowBack.png"));
            Image scaledBackArrow = originalBackArrow.getImage().getScaledInstance(ARROW_SIZE, ARROW_SIZE, Image.SCALE_SMOOTH);
            backArrowIcon = new ImageIcon(scaledBackArrow);
            
            // Load star icons
            ImageIcon originalFilledStar = new ImageIcon(getClass().getResource("/gameproject/resources/filledStar.png"));
            Image scaledFilledStar = originalFilledStar.getImage().getScaledInstance(STAR_SIZE, STAR_SIZE, Image.SCALE_SMOOTH);
            filledStarIcon = new ImageIcon(scaledFilledStar);
            
            ImageIcon originalEmptyStar = new ImageIcon(getClass().getResource("/gameproject/resources/emptyStar.png"));
            Image scaledEmptyStar = originalEmptyStar.getImage().getScaledInstance(STAR_SIZE, STAR_SIZE, Image.SCALE_SMOOTH);
            emptyStarIcon = new ImageIcon(scaledEmptyStar);
            
            // Load and resize level box images
            for (int i = 0; i < 3; i++) {
                String resourcePath = "/gameproject/resources/level" + (i+1) + ".png";

                // Try uppercase with underscore if standard name doesn't work
                if (getClass().getResource(resourcePath) == null) {
                    resourcePath = "/gameproject/resources/LEVEL_" + (i+1) + ".png";
                }

                ImageIcon originalIcon = new ImageIcon(getClass().getResource(resourcePath));
                Image scaledImage = originalIcon.getImage().getScaledInstance(LEVEL_BOX_SIZE, LEVEL_BOX_SIZE, Image.SCALE_SMOOTH);
                levelBoxIcons[i] = new ImageIcon(scaledImage);
            }
            
            // Load and resize level locked image
            ImageIcon originalLockedIcon = new ImageIcon(getClass().getResource("/gameproject/resources/level_locked.png"));
            Image scaledLockedImage = originalLockedIcon.getImage().getScaledInstance(LEVEL_BOX_SIZE, LEVEL_BOX_SIZE, Image.SCALE_SMOOTH);
            lockedLevelIcon = new ImageIcon(scaledLockedImage);
            
        } catch (Exception e) {
            System.err.println("Error loading resources: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to default font
            pixelifyFont = new Font("Arial", Font.BOLD, 12);
            
            // Create fallback level locked icon if not found
            if (lockedLevelIcon == null) {
                // Create a simple lock icon with chains
                BufferedImage lockImg = new BufferedImage(LEVEL_BOX_SIZE, LEVEL_BOX_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = lockImg.createGraphics();
                
                // Semi-transparent dark overlay
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRect(0, 0, LEVEL_BOX_SIZE, LEVEL_BOX_SIZE);
                
                // Draw a lock and chains
                g2d.setColor(Color.GRAY);
                g2d.fillRoundRect(LEVEL_BOX_SIZE/2 - 25, LEVEL_BOX_SIZE/2 - 15, 50, 60, 10, 10);
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillRoundRect(LEVEL_BOX_SIZE/2 - 15, LEVEL_BOX_SIZE/2 - 5, 30, 40, 5, 5);
                
                // Chains
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.setStroke(new BasicStroke(4f));
                // Left chain
                for (int i = 0; i < 3; i++) {
                    g2d.drawOval(30 + (i*15), 50, 10, 15);
                }
                // Right chain
                for (int i = 0; i < 3; i++) {
                    g2d.drawOval(LEVEL_BOX_SIZE - 40 - (i*15), 50, 10, 15);
                }
                
                g2d.dispose();
                lockedLevelIcon = new ImageIcon(lockImg);
            }
        }
    }
    
    /**
     * Update the level status based on progress
     */
    public void updateLevelStatus() {
        // Define difficulty strings for each level
        String[] difficulties = {"Beginner", "Intermediate", "Advanced"};

        // Force the progress tracker to reload progress
        controller.progressTracker.saveProgress();

        // For each level, update the star display
        for (int i = 0; i < 3; i++) {
            String difficulty = difficulties[i];
            int levelNum = 1; // Always 1 since each level is the first of its difficulty

            // Log the completion check for debugging
            System.out.println("DEBUG: Checking completion for " + difficulty + " level " + levelNum);
            boolean isCompleted = controller.isLevelCompleted(difficulty, levelNum);
            System.out.println("DEBUG: Level completed: " + isCompleted);

            // Update stars based on completion
            if (isCompleted) {
                // Get stars earned
                int starsEarned = controller.getStarsForLevel(difficulty, levelNum);
                System.out.println("DEBUG: Stars earned: " + starsEarned);

                // Update star display
                for (int j = 0; j < 3; j++) {
                    if (j < starsEarned) {
                        starLabels[i][j].setIcon(filledStarIcon);
                    } else {
                        starLabels[i][j].setIcon(emptyStarIcon);
                    }
                }
            } else {
                // Level not completed, show empty stars
                for (int j = 0; j < 3; j++) {
                    starLabels[i][j].setIcon(emptyStarIcon);
                }
            }
            
            // Update lock icons visibility
            if (i == 0) {
                // Level 1 is always unlocked
                lockIcons[i].setVisible(false);
            } else if (i == 1) {
                // Level 2 is unlocked if Level 1 is completed
                lockIcons[i].setVisible(!controller.isLevelCompleted("Beginner", 1));
            } else if (i == 2) {
                // Level 3 is unlocked if Level 2 is completed
                lockIcons[i].setVisible(!controller.isLevelCompleted("Intermediate", 1));
            }
        }

        // Force repaint to update UI
        mainPanel.repaint();
    }
    
    /**
     * Custom panel that paints the background image
     */
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (backgroundImage != null) {
                // Draw background image scaled to fit the panel
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            } else {
                // Fallback to a blue gradient if image is not available
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 80), 
                    0, getHeight(), new Color(5, 5, 30));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}