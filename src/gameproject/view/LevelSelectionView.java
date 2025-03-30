package gameproject.view;

import gameproject.controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * View for the level selection screen styled exactly according to the reference image
 */
public class LevelSelectionView extends JPanel {
    private GameController controller;
    private Font pixelifyFont;
    private ImageIcon backgroundImage;
    private ImageIcon filledStarIcon;
    private ImageIcon emptyStarIcon;
    private ImageIcon[] levelBoxIcons = new ImageIcon[3];
    private ImageIcon backArrowIcon;
    
    // Constants for sizing - adjusted for better screen positioning
    private static final int LEVEL_BOX_SIZE = 150;   // Size for level boxes
    private static final int STAR_SIZE = 32;        // Size for stars
    private static final int LEVEL_SPACING = 50;    // Horizontal spacing between levels
    private static final int TOP_PADDING = 80;      // More space at the top to center vertically
    private static final int ARROW_SIZE = 40;       // Size for back arrow (reduced)
    
    /**
     * Constructor - Initialize the level selection view to match the reference image
     */
    public LevelSelectionView(GameController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        
        // Load resources
        loadResources();
        
        // Create main panel with background
        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // Add the main panel to this view
        add(mainPanel);
        
        // Create a container for all elements (title + levels)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Add top padding to push content down toward center
        contentPanel.add(Box.createRigidArea(new Dimension(0, TOP_PADDING)));
        
        // Create title label with larger font
        JLabel titleLabel = new JLabel("SELECT A LEVEL");
        titleLabel.setFont(pixelifyFont.deriveFont(Font.BOLD, 36f));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 60, 0)); // More space below title
        contentPanel.add(titleLabel);
        
        // Create the level selection area
        JPanel levelSelectionPanel = createLevelSelectionPanel();
        levelSelectionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(levelSelectionPanel);
        
        // Add flexible space at the bottom to help with vertical centering
        contentPanel.add(Box.createVerticalGlue());
        
        // Add back button as a smaller image
        JLabel backButton = new JLabel(backArrowIcon);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.showMainMenu();
            }
        });
        
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));  // Added padding
        backButtonPanel.setOpaque(false);
        backButtonPanel.add(backButton);
        
        mainPanel.add(backButtonPanel, BorderLayout.WEST);
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
            
            // Load and resize star icons
            ImageIcon originalFilledStar = new ImageIcon(getClass().getResource("/gameproject/resources/filledStar.png"));
            ImageIcon originalEmptyStar = new ImageIcon(getClass().getResource("/gameproject/resources/emptyStar.png"));
            
            // Scale star icons to the specified size
            Image scaledFilledStar = originalFilledStar.getImage().getScaledInstance(STAR_SIZE, STAR_SIZE, Image.SCALE_SMOOTH);
            filledStarIcon = new ImageIcon(scaledFilledStar);
            
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
        } catch (Exception e) {
            System.err.println("Error loading resources: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to default font
            pixelifyFont = new Font("Arial", Font.BOLD, 12);
        }
    }
    
    /**
     * Create the level selection panel with exactly 3 levels as shown in the reference image
     */
    private JPanel createLevelSelectionPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, LEVEL_SPACING, 0)); // Use constant for spacing
        panel.setOpaque(false);
        
        // Create exactly 3 level boxes with stars above as shown in the image
        for (int i = 0; i < 3; i++) {
            final int level = i + 1;
            
            // Create a container for each level (stars + level box)
            JPanel levelContainer = new JPanel();
            levelContainer.setLayout(new BoxLayout(levelContainer, BoxLayout.Y_AXIS));
            levelContainer.setOpaque(false);
            
            // Create star row with 3 stars
            JPanel starRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
            starRow.setOpaque(false);
            
            // Add 3 stars above each level box
            // Level 1 has 3 filled stars, Level 2 has no filled stars, Level 3 has 3 filled stars (based on image)
            for (int j = 0; j < 3; j++) {
                JLabel starLabel;
                if ((level == 1 || level == 3) && emptyStarIcon != null) {
                    starLabel = new JLabel(filledStarIcon);
                } else {
                    starLabel = new JLabel(emptyStarIcon);
                }
                starRow.add(starLabel);
            }
            
            // Add star row to level container
            levelContainer.add(starRow);
            
            // Add spacing between stars and level box
            levelContainer.add(Box.createRigidArea(new Dimension(0, 5)));
            
            // Create clickable level box with fixed size
            JPanel levelBox = new JPanel(new BorderLayout());
            levelBox.setOpaque(false);
            levelBox.setPreferredSize(new Dimension(LEVEL_BOX_SIZE, LEVEL_BOX_SIZE));
            
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
                    controller.startLevel("Beginner", level);
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    // Subtle highlight effect
                    boxImageLabel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 1));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    boxImageLabel.setBorder(null);
                }
            });
            
            // Add level box to container
            levelContainer.add(levelBox);
            
            // Add level container to panel
            panel.add(levelContainer);
        }
        
        return panel;
    }
    
    
    /**
    * Update the level status based on progress
    */
    public void updateLevelStatus() {
       // This method should update the UI elements in the LevelSelectionView
       // based on the player's progress

       // For now, just repaint the view
       revalidate();
       repaint();
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