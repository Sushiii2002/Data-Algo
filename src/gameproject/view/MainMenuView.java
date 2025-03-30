package gameproject.view;

import gameproject.controller.GameController;
import gameproject.util.ResourceManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Enhanced main menu view with custom background and animated buttons
 */
public class MainMenuView extends JPanel {
    private GameController controller;
    private ResourceManager resourceManager;
    private ImageIcon backgroundImage;
    private ImageIcon normalButtonImage;
    private ImageIcon hoverButtonImage;
    private ImageIcon clickedButtonImage;
    private Font titleFont;
    private Font subtitleFont;
    private Font buttonFont;
    
    /**
     * Constructor - Initialize the main menu view
     */
    public MainMenuView(GameController controller) {
        this.controller = controller;
        this.resourceManager = ResourceManager.getInstance();
        
        // Load resources
        loadResources();
        
        // Set layout
        setLayout(new BorderLayout());
        
        // Create a background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        add(backgroundPanel, BorderLayout.CENTER);
        
        // Set up the content in the background panel
        setupContent(backgroundPanel);
    }
    
    /**
     * Load all required resources
     */
    private void loadResources() {
        try {
            // Load background image
            backgroundImage = resourceManager.getImage("/gameproject/resources/mainmenu.png");
            
            // Load button images
            normalButtonImage = resourceManager.getImage("/gameproject/resources/NormalButton.png");
            hoverButtonImage = resourceManager.getImage("/gameproject/resources/HoverButton.png");
            clickedButtonImage = resourceManager.getImage("/gameproject/resources/ClickedButton.png");
            
            // Load fonts - using default font paths, adjust as needed
            titleFont = new Font("Arial", Font.BOLD, 56);
            subtitleFont = new Font("Arial", Font.ITALIC, 24);
            buttonFont = new Font("Arial", Font.BOLD, 28);
            
            // Try to load custom font if available
            try {
                Font customFont = resourceManager.getFont("/gameproject/resources/PixelifySans.ttf", 24f);
                if (customFont != null) {
                    buttonFont = customFont;
                    titleFont = customFont.deriveFont(Font.BOLD, 24f);
                    subtitleFont = customFont.deriveFont(Font.ITALIC, 54f);
                }
            } catch (Exception e) {
                System.err.println("Could not load custom font, using defaults");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading resources: " + e.getMessage());
        }
    }
    
    /**
     * Set up the content components
     */
    private void setupContent(JPanel parent) {
        parent.setLayout(new BoxLayout(parent, BoxLayout.Y_AXIS));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        // Game title with specified color (#DE8A41)
        JLabel titleLabel = new JLabel("QUESTBOUND:");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(new Color(0xDE, 0x8A, 0x41)); 
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Mysteries Unveiled");
        subtitleLabel.setFont(subtitleFont);
        subtitleLabel.setForeground(new Color(0xDE, 0x8A, 0x41)); 
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(Box.createVerticalGlue());
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 50)));
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        // Create custom animated buttons
        AnimatedButton startButton = createAnimatedButton("START GAME");
        AnimatedButton levelsButton = createAnimatedButton("LEVELS");
        AnimatedButton helpButton = createAnimatedButton("HOW TO PLAY");
        AnimatedButton exitButton = createAnimatedButton("EXIT");
        
        // Add action listeners
        startButton.addActionListener(e -> controller.startGame());
        levelsButton.addActionListener(e -> controller.showLevelSelection());
        helpButton.addActionListener(e -> controller.showHelp());
        exitButton.addActionListener(e -> controller.exitGame());
        
        // Add buttons to panel with spacing
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(startButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(levelsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(helpButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createVerticalGlue());
        
        // Add components to parent panel
        parent.add(Box.createVerticalGlue());
        parent.add(titlePanel);
        parent.add(buttonPanel);
        parent.add(Box.createVerticalGlue());
    }
    
    /**
     * Create a custom animated button
     */
    private AnimatedButton createAnimatedButton(String text) {
        AnimatedButton button = new AnimatedButton(text, normalButtonImage, hoverButtonImage, clickedButtonImage);
        // Apply font with explicit derivation to ensure proper sizing
        button.setFont(buttonFont.deriveFont(34f));
        button.setForeground(Color.WHITE);
        button.setMaximumSize(new Dimension(300, 70));
        button.setPreferredSize(new Dimension(300, 70));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }
    
    /**
     * Custom panel that paints the background image
     */
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            if (backgroundImage != null) {
                // Scale the image to fit the panel
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            } else {
                // Fallback to a gradient background
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 20, 60), 
                    0, getHeight(), new Color(0, 60, 100));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        }
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
}