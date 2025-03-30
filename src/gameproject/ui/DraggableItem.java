package gameproject.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A draggable component for sorting visualizations
 */
public class DraggableItem extends JPanel {
    private int value;
    private Color color;
    private boolean isDragging = false;
    private Point dragOffset;
    private Point originalPosition;
    private DragListener dragListener;
    
    /**
     * Constructor
     */
    public DraggableItem(int value, Color color) {
        this.value = value;
        this.color = color;
        this.setOpaque(true);
        this.setBackground(color);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        // Set preferred size proportional to value
        int width = 40;
        int height = 30 + (value * 10);
        this.setPreferredSize(new Dimension(width, height));
        this.setSize(width, height);
        
        // Add mouse listeners for drag and drop
        DragAdapter adapter = new DragAdapter();
        this.addMouseListener(adapter);
        this.addMouseMotionListener(adapter);
    }
    
    /**
     * Paint the component
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Set rendering hints for better text quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw the value
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        
        String valueStr = String.valueOf(value);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(valueStr)) / 2;
        int textY = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        
        g2d.drawString(valueStr, textX, textY);
    }
    
    /**
     * Get the item's value
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Set drag listener
     */
    public void setDragListener(DragListener listener) {
        this.dragListener = listener;
    }
    
    /**
     * Get original position
     */
    public Point getOriginalPosition() {
        return originalPosition;
    }
    
    /**
     * Set original position
     */
    public void setOriginalPosition(Point pos) {
        this.originalPosition = pos;
    }
    
    /**
     * Interface for drag events
     */
    public interface DragListener {
        void onDragStart(DraggableItem item);
        void onDragEnd(DraggableItem item);
        void onDragging(DraggableItem item, Point currentPos);
    }
    
    /**
     * Adapter for mouse events
     */
    private class DragAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            isDragging = true;
            dragOffset = e.getPoint();
            originalPosition = getLocation();
            
            if (dragListener != null) {
                dragListener.onDragStart(DraggableItem.this);
            }
            
            // Move this component to the front
            Container parent = getParent();
            if (parent != null) {
                parent.setComponentZOrder(DraggableItem.this, 0);
                parent.repaint();
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            isDragging = false;
            
            if (dragListener != null) {
                dragListener.onDragEnd(DraggableItem.this);
            }
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (isDragging) {
                Point currentPosition = getLocation();
                int newX = currentPosition.x + e.getX() - dragOffset.x;
                int newY = currentPosition.y + e.getY() - dragOffset.y;
                setLocation(newX, newY);
                
                if (dragListener != null) {
                    dragListener.onDragging(DraggableItem.this, new Point(newX, newY));
                }
            }
        }
    }
}