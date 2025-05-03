package gameproject.ui;

import gameproject.model.LevelConfig;

import gameproject.util.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 * Grid-based game board for sorting challenges
 * Using styled grid even if images are missing
 */
public class GameGrid extends JPanel {
    private ResourceManager resourceManager;
    private LevelConfig currentLevel;
    private ImageIcon gridBoxImage;  // Individual box image
    private ImageIcon gridBgImage;   // Background for the grid
    private int gridSize;
    private int cellSize; // For 1:1 ratio
    
    // Constants for grid padding
    private final double HORIZONTAL_PADDING_PERCENT = 0.15; // 15% padding on each side
    private final double VERTICAL_PADDING_PERCENT = 0.15;   // 15% padding on top and bottom
    
    // Border size as percentage of grid size
    private final double BORDER_SIZE_PERCENT = 0.08; // 8% of grid size
    
    // Horizontal offset for the entire grid (positive = right, negative = left)
    private final int HORIZONTAL_OFFSET = 300; // pixels to the right
    
    // Fallback colors if images are missing
    private final Color GRID_BORDER_COLOR = new Color(165, 82, 71);      // Reddish-brown border
    private final Color GRID_BACKGROUND_COLOR = new Color(245, 222, 179); // Wheat/tan color for background
    private final Color GRID_LINE_COLOR = new Color(193, 125, 99);        // Lighter reddish-brown for gridlines
    private final Color GRID_CORNER_COLOR = new Color(132, 66, 57);       // Darker color for corners
    
    /**
     * Constructor
     */
    public GameGrid(LevelConfig level) {
        this.resourceManager = ResourceManager.getInstance();
        this.currentLevel = level;
        this.gridSize = level.getGridSize();
        
        // Load grid images
        this.gridBgImage = resourceManager.getImage("/gameproject/resources/grid_bg.png");
        this.gridBoxImage = resourceManager.getImage("/gameproject/resources/grid_box.png");
        
        // Log image loading status
        if (gridBgImage == null) {
            System.out.println("WARNING: grid_bg.png could not be loaded");
        }
        if (gridBoxImage == null) {
            System.out.println("WARNING: grid_box.png could not be loaded");
        }
        
        setLayout(null);
        setOpaque(false);
        
        // Calculate the cell size and grid positioning when the panel is resized
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repaint();
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calculate grid metrics
        int horizontalPadding = (int)(getWidth() * HORIZONTAL_PADDING_PERCENT);
        int verticalPadding = (int)(getHeight() * VERTICAL_PADDING_PERCENT);
        
        // Calculate available space for the grid
        int availableWidth = getWidth() - (horizontalPadding * 2);
        int availableHeight = getHeight() - (verticalPadding * 2);
        
        // Calculate cell size to fit within the padded area while maintaining 1:1 ratio
        int maxCellWidth = availableWidth / gridSize;
        int maxCellHeight = availableHeight / gridSize;
        cellSize = Math.min(maxCellWidth, maxCellHeight);
        
        // Calculate grid dimensions
        int totalGridWidth = cellSize * gridSize;
        int totalGridHeight = cellSize * gridSize;
        
        // Calculate border size
        int borderSize = (int)(totalGridWidth * BORDER_SIZE_PERCENT);
        if (borderSize < 6) borderSize = 6;  // Minimum border size
        
        // Calculate grid start position with horizontal offset
        int gridStartX = horizontalPadding + (availableWidth - totalGridWidth) / 2 + HORIZONTAL_OFFSET;
        int gridStartY = verticalPadding + (availableHeight - totalGridHeight) / 2;
        
        // Add boundary checks to prevent grid from being pushed outside the visible area
        int maxX = getWidth() - totalGridWidth - borderSize * 2;
        if (gridStartX > maxX) {
            gridStartX = maxX;
        }
        
        // Ensure grid doesn't go off the left edge
        if (gridStartX < borderSize) {
            gridStartX = borderSize;
        }
        
        // If gridBgImage exists, draw it larger than the grid to create a border with gap
        if (gridBgImage != null) {
            g2d.drawImage(gridBgImage.getImage(), 
                    gridStartX - borderSize, 
                    gridStartY - borderSize, 
                    totalGridWidth + (borderSize * 2), 
                    totalGridHeight + (borderSize * 2), 
                    this);
        } else {
            // Fallback: Draw styled grid background if image is missing
            drawStyledGridBackground(g2d, gridStartX, gridStartY, totalGridWidth, totalGridHeight, borderSize);
        }
        
        // Draw grid background if no grid box images
        if (gridBoxImage == null) {
            g2d.setColor(GRID_BACKGROUND_COLOR);
            g2d.fillRect(gridStartX, gridStartY, totalGridWidth, totalGridHeight);
        }
        
        // Draw individual grid boxes with 1:1 ratio or draw grid lines
        if (gridBoxImage != null) {
            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    int x = gridStartX + (col * cellSize);
                    int y = gridStartY + (row * cellSize);
                    
                    g2d.drawImage(gridBoxImage.getImage(), x, y, cellSize, cellSize, this);
                }
            }
        } else {
            // Draw grid lines if grid box image is missing
            drawGridLines(g2d, gridStartX, gridStartY, totalGridWidth, totalGridHeight);
        }
    }
    
    /**
     * Fallback method to draw a styled grid background if image is missing
     */
    private void drawStyledGridBackground(Graphics2D g2d, int x, int y, int width, int height, int borderSize) {
        // Draw grid border (reddish-brown rectangle)
        g2d.setColor(GRID_BORDER_COLOR);
        g2d.fillRect(
            x - borderSize, 
            y - borderSize, 
            width + borderSize * 2, 
            height + borderSize * 2
        );
        
        // Draw corner decorations
        int cornerSize = borderSize * 2;
        if (cornerSize < 8) cornerSize = 8;
        
        g2d.setColor(GRID_CORNER_COLOR);
        
        // Top-left corner
        g2d.fillRect(x - borderSize, y - borderSize, cornerSize, cornerSize);
        
        // Top-right corner
        g2d.fillRect(x + width + borderSize - cornerSize, y - borderSize, cornerSize, cornerSize);
        
        // Bottom-left corner
        g2d.fillRect(x - borderSize, y + height + borderSize - cornerSize, cornerSize, cornerSize);
        
        // Bottom-right corner
        g2d.fillRect(x + width + borderSize - cornerSize, y + height + borderSize - cornerSize, cornerSize, cornerSize);
    }
    
    /**
     * Fallback method to draw grid lines if images are missing
     */
    private void drawGridLines(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(GRID_LINE_COLOR);
        
        // Draw horizontal grid lines
        for (int row = 0; row <= gridSize; row++) {
            int lineY = y + (row * cellSize);
            g2d.drawLine(x, lineY, x + width, lineY);
        }
        
        // Draw vertical grid lines
        for (int col = 0; col <= gridSize; col++) {
            int lineX = x + (col * cellSize);
            g2d.drawLine(lineX, y, lineX, y + height);
        }
    }
    
    /**
     * Get the cell size (useful for positioning items)
     */
    public int getCellSize() {
        return cellSize;
    }
    
    /**
     * Get the grid start X coordinate (useful for positioning items)
     * Includes boundary checking
     */
    public int getGridStartX() {
        int horizontalPadding = (int)(getWidth() * HORIZONTAL_PADDING_PERCENT);
        int availableWidth = getWidth() - (horizontalPadding * 2);
        int totalGridWidth = cellSize * gridSize;
        int borderSize = (int)(totalGridWidth * BORDER_SIZE_PERCENT);
        if (borderSize < 6) borderSize = 6;
        
        int x = horizontalPadding + (availableWidth - totalGridWidth) / 2 + HORIZONTAL_OFFSET;
        
        // Add boundary checks
        int maxX = getWidth() - totalGridWidth - borderSize * 2;
        if (x > maxX) {
            x = maxX;
        }
        if (x < borderSize) {
            x = borderSize;
        }
        
        return x;
    }
    
    /**
     * Get the grid start Y coordinate (useful for positioning items)
     */
    public int getGridStartY() {
        int verticalPadding = (int)(getHeight() * VERTICAL_PADDING_PERCENT);
        int availableHeight = getHeight() - (verticalPadding * 2);
        int totalGridHeight = cellSize * gridSize;
        return verticalPadding + (availableHeight - totalGridHeight) / 2;
    }
    
    /**
     * Get the current state of items as an array of values
     */
    public int[] getCurrentState() {
        // Return empty array for now until we implement the items
        return new int[0];
    }
}