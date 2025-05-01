package gameproject.view;

import gameproject.controller.GameController;
import gameproject.util.GameConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * View for the level selection screen with improved centering and no stars
 */
public class LevelSelectionView extends JPanel {
    private GameController controller;
    private Font pixelifyFont;
    private ImageIcon backgroundImage;
    private ImageIcon[] levelBoxIcons = new ImageIcon[3];
    private ImageIcon backArrowIcon;
    
    // Constants for sizing - adjusted for better screen positioning
    private static final int LEVEL_BOX_SIZE = 150;   // Size for level boxes
    private static final int LEVEL_SPACING = 50;     // Horizontal spacing between levels
    private static final int ARROW_SIZE = 40;        // Size for back arrow
    
    /**
     * Constructor - Initialize the level selection view with centered elements
     */
    public LevelSelectionView(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        
        // Load resources
        loadResources();
        
        // Create main panel with background
        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(null); // Use null layout for precise positioning
        
        // Add the main panel to this view
        add(mainPanel);
        
        // Create title label with larger font - precisely centered
        JLabel titleLabel = new JLabel("SELECT A LEVEL", JLabel.CENTER);
        titleLabel.setFont(pixelifyFont.deriveFont(Font.BOLD, 36f));
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
            
            // Create level box panel
            JPanel levelBox = new JPanel(new BorderLayout());
            levelBox.setOpaque(false);
            levelBox.setBounds(boxX, boxesY, LEVEL_BOX_SIZE, LEVEL_BOX_SIZE);
            
            // Use level box image
            JLabel boxImageLabel = new JLabel(levelBoxIcons[i]);
            boxImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Add box image to level box
            levelBox.add(boxImageLabel, BorderLayout.CENTER);
            
            // Make box clickable
            levelBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
            levelBox.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // For Level 2, set the game level properly
                    if (level == 2) {
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
                    } else {
                        // For other levels, use the default behavior
                        controller.model.setGameLevel(level);
                        controller.startLevel("Beginner", level);
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
        } catch (Exception e) {
            System.err.println("Error loading resources: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to default font
            pixelifyFont = new Font("Arial", Font.BOLD, 12);
        }
    }
    
    /**
     * Update the level status based on progress
     * This method is kept for compatibility but doesn't show stars anymore
     */
    public void updateLevelStatus() {
        // Method kept for interface compatibility, but doesn't do anything
        // since we've removed the stars from the UI
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