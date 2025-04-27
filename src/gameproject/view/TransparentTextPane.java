package gameproject.view;

import javax.swing.*;
import javax.swing.text.html.*;
import java.awt.*;

/**
 * A custom text pane that properly renders HTML content with transparency
 */
public class TransparentTextPane extends JTextPane {
    
    /**
     * Create a new transparent text pane
     */
    public TransparentTextPane() {
        // Basic setup
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setBorder(null);
        setEditable(false);
        setFocusable(false);
        
        // Configure for HTML
        setContentType("text/html");
        
        // Apply style rules
        try {
            HTMLEditorKit kit = (HTMLEditorKit)getEditorKit();
            StyleSheet styleSheet = kit.getStyleSheet();
            styleSheet.addRule("body { background-color: transparent; color: white; font-family: SansSerif; font-size: 18pt; }");
            styleSheet.addRule("p { background-color: transparent; margin: 0; }");
        } catch (Exception e) {
            System.err.println("Error configuring HTML styles: " + e.getMessage());
        }
    }
    
    /**
     * Set HTML text with proper formatting
     */
    public void setHtmlText(String text) {
        try {
            // Format properly as HTML document
            setText("<html><body>" + text + "</body></html>");
        } catch (Exception e) {
            System.err.println("Error setting HTML text: " + e.getMessage());
            // Fallback to plain text
            setText(text);
        }
    }
    
    /**
     * Override to ensure transparency
     */
    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}