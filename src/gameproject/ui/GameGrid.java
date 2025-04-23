// src/gameproject/ui/GameGrid.java

package gameproject.ui;

import gameproject.model.LevelConfig;
import gameproject.model.GameState;
import gameproject.util.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Grid-based game board for sorting challenges
 */
public class GameGrid extends JPanel {
    private ResourceManager resourceManager;
    private LevelConfig currentLevel;
    private ImageIcon gridBoxImage;  // Individual box image
    private ImageIcon gridBgImage;   // Background for the grid
    private int gridSize;
    private int cellSize; // For 1:1 ratio
    
    // Constants for grid padding (percentage of total size)
    private final double HORIZONTAL_PADDING_PERCENT = 0.08; // 8% padding on each side
    private final double VERTICAL_PADDING_PERCENT = 0.08;   // 8% padding on top and bottom
    
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
        
        // Draw the grid background for the entire panel
        if (gridBgImage != null) {
            g.drawImage(gridBgImage.getImage(), 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(139, 69, 19)); // Brown fallback
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
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
        
        // Calculate grid start position to center it within the padded area
        int totalGridWidth = cellSize * gridSize;
        int totalGridHeight = cellSize * gridSize;
        
        int gridStartX = horizontalPadding + (availableWidth - totalGridWidth) / 2;
        int gridStartY = verticalPadding + (availableHeight - totalGridHeight) / 2;
        
        // Draw individual grid boxes with 1:1 ratio
        if (gridBoxImage != null) {
            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    int x = gridStartX + (col * cellSize);
                    int y = gridStartY + (row * cellSize);
                    
                    g.drawImage(gridBoxImage.getImage(), x, y, cellSize, cellSize, this);
                }
            }
        }
    }
    
    /**
     * Get the current state of items as an array of values
     */
    public int[] getCurrentState() {
        // Return empty array for now until we implement the items
        return new int[0];
    }
}