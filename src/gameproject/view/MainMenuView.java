package gameproject.view;

import gameproject.controller.GameController;
import gameproject.util.ResourceManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

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
        helpButton.addActionListener(e -> showHowToPlayOverlay());
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
    
    
    
    
    
    
    
    /**
    * Shows a "How to Play" overlay using the glass pane approach
    */
   private void showHowToPlayOverlay() {
       // Get the root window/frame
       Window window = SwingUtilities.getWindowAncestor(this);
       if (!(window instanceof JFrame)) {
           // Fallback to standard dialog if not in a JFrame
           controller.showHelp();
           return;
       }

       JFrame frame = (JFrame) window;

       // Create a glass pane panel
       JPanel glassPanel = new JPanel() {
           @Override
           protected void paintComponent(Graphics g) {
               super.paintComponent(g);
               // Semi-transparent dark overlay (85% opacity black)
               g.setColor(new Color(0, 0, 0, 217));
               g.fillRect(0, 0, getWidth(), getHeight());
           }
       };
       glassPanel.setOpaque(false);
       glassPanel.setLayout(null);

       // Create the content panel with pixelated golden border
       JPanel contentPanel = new JPanel() {
           @Override
           protected void paintComponent(Graphics g) {
               super.paintComponent(g);
               Graphics2D g2d = (Graphics2D) g;

               // Main panel background - dark navy blue
               g2d.setColor(new Color(15, 20, 50, 230));
               g2d.fillRect(0, 0, getWidth(), getHeight());

               // Golden border
               g2d.setColor(new Color(255, 215, 0));
               g2d.setStroke(new BasicStroke(3f));
               g2d.drawRect(2, 2, getWidth()-4, getHeight()-4);
           }
       };

       // Position and size content panel
       int panelWidth = 600;  // Fixed width
       int panelHeight = 400; // Fixed height
       contentPanel.setBounds(
           (frame.getWidth() - panelWidth)/2, 
           (frame.getHeight() - panelHeight)/2, 
           panelWidth, 
           panelHeight
       );
       contentPanel.setOpaque(false);
       contentPanel.setLayout(null);
       glassPanel.add(contentPanel);

       // Add title
       JLabel titleLabel = new JLabel("HOW TO PLAY", JLabel.CENTER);
       // Use the pixelifySansFont if available
       if (buttonFont != null) {
           titleLabel.setFont(buttonFont.deriveFont(36f));
       } else {
           titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
       }
       titleLabel.setForeground(new Color(255, 215, 0));
       titleLabel.setBounds(0, 20, panelWidth, 50);
       contentPanel.add(titleLabel);

       // Add decorative divider
       JPanel divider = new JPanel() {
           @Override
           protected void paintComponent(Graphics g) {
               super.paintComponent(g);
               Graphics2D g2d = (Graphics2D) g;
               g2d.setColor(new Color(255, 215, 0));
               g2d.setStroke(new BasicStroke(2f));
               g2d.drawLine(panelWidth/6, 1, panelWidth*5/6, 1);
           }
       };
       divider.setOpaque(false);
       divider.setBounds(0, 75, panelWidth, 4);
       contentPanel.add(divider);

       // Create text content with proper colored text for each section
       String htmlContent = 
           "<html><body style='width: " + (panelWidth - 80) + "px'>" +
           "<div style='font-size: 14px; color: white; font-family: Arial, sans-serif;'>" +
           "<p>The Alchemist's Path is an interactive RPG that teaches the TimSort algorithm through potion crafting.</p>" +
           "<p><b>Three essential abilities you'll master:</b></p>" +
           "<p style='margin-left: 20px;'>- <span style='color: #80C8FF;'>The Eye of Pattern:</span> Identify natural sequences in ingredients</p>" +
           "<p style='margin-left: 20px;'>- <span style='color: #FFB280;'>The Hand of Balance:</span> Sort small groups of ingredients</p>" +
           "<p style='margin-left: 20px;'>- <span style='color: #80FF9E;'>The Mind of Unity:</span> Merge ordered ingredients to craft potions</p>" +
           "<p><b>Create the right potions to defeat three powerful bosses:</b></p>" +
           "<p style='margin-left: 20px;'>- <span style='color: #FF8080;'>Flameclaw:</span> A fire elemental that burns everything</p>" +
           "<p style='margin-left: 20px;'>- <span style='color: #A0FF80;'>Toxitar:</span> A poison beast that spreads corruption</p>" +
           "<p style='margin-left: 20px;'>- <span style='color: #C080FF;'>Lord Chaosa:</span> A reality-warping final boss</p>" +
           "<p>Follow the story and character hints to choose the correct potions!</p>" +
           "</div></body></html>";

       JLabel textLabel = new JLabel(htmlContent);
       textLabel.setVerticalAlignment(JLabel.TOP);
       textLabel.setBounds(40, 100, panelWidth - 80, panelHeight - 180);
       contentPanel.add(textLabel);

       // Create an OK button using the animated button class - THIS IS THE KEY FIX
       AnimatedButton okButton = createAnimatedButton("OK");
       okButton.setMaximumSize(new Dimension(150, 50));
       okButton.setPreferredSize(new Dimension(150, 50));
       okButton.setBounds((panelWidth - 150)/2, panelHeight - 70, 150, 50);
       okButton.addActionListener(e -> {
           // Restore original glass pane
           frame.setGlassPane(frame.getGlassPane());
           frame.getGlassPane().setVisible(false);
       });
       contentPanel.add(okButton);

       // Make sure clicks on glass pane outside content panel close the overlay
       glassPanel.addMouseListener(new MouseAdapter() {
           @Override
           public void mouseClicked(MouseEvent e) {
               Point p = SwingUtilities.convertPoint(glassPanel, e.getPoint(), contentPanel);
               if (p.x < 0 || p.y < 0 || p.x > contentPanel.getWidth() || p.y > contentPanel.getHeight()) {
                   // Restore original glass pane
                   frame.setGlassPane(frame.getGlassPane());
                   frame.getGlassPane().setVisible(false);
               }
               e.consume(); // Critical: consume event to prevent it from reaching components beneath
           }

           @Override
           public void mousePressed(MouseEvent e) {
               e.consume(); // Consume all mouse events that hit the glass pane
           }

           @Override
           public void mouseReleased(MouseEvent e) {
               e.consume();
           }
       });

       // Store the original glass pane
       Component originalGlassPane = frame.getGlassPane();

       // Set and show our glass pane
       frame.setGlassPane(glassPanel);
       glassPanel.setVisible(true);
   }






    // Helper method to create a potion icon as fallback
    private ImageIcon createPotionIcon(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw flask
        g2d.setColor(new Color(50, 200, 255, 200));
        int neckWidth = width/3;
        int bottleHeight = (int)(height * 0.7);

        // Draw neck
        g2d.fillRect((width - neckWidth)/2, 5, neckWidth, height/4);

        // Draw bottle
        g2d.fillRoundRect(10, height/4, width - 20, bottleHeight, 20, 20);

        // Draw liquid
        g2d.setColor(new Color(100, 50, 200, 180));
        g2d.fillRoundRect(13, height/2, width - 26, bottleHeight/2, 18, 18);

        // Draw bubbles
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.fillOval(width/2 - 5, height/2 + 10, 10, 10);
        g2d.fillOval(width/3, height/2 + 20, 6, 6);
        g2d.fillOval(2*width/3, height/2 + 15, 8, 8);

        g2d.dispose();
        return new ImageIcon(img);
    }
    
}