package gameproject.util;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class for managing game resources
 */
public class ResourceManager {
    private static final Logger LOGGER = Logger.getLogger(ResourceManager.class.getName());
    private static ResourceManager instance;
    
    private final Map<String, ImageIcon> imageCache = new HashMap<>();
    private final Map<String, Font> fontCache = new HashMap<>();
    
    // Private constructor for singleton
    private ResourceManager() {
        // Initialize logger
        System.setProperty("java.util.logging.SimpleFormatter.format", 
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }
    
    /**
     * Load and cache an image
     */
    public ImageIcon getImage(String path) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }
        
        try {
            // Normalize path (ensure it starts with /)
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            
            ImageIcon icon = new ImageIcon(getClass().getResource(path));
            imageCache.put(path, icon);
            LOGGER.info("Loaded image: " + path);
            return icon;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load image: " + path, e);
            return null;
        }
    }
    
    /**
     * Load and cache a resized image
     */
    public ImageIcon getResizedImage(String path, int width, int height) {
        String cacheKey = path + "_" + width + "x" + height;
        
        if (imageCache.containsKey(cacheKey)) {
            return imageCache.get(cacheKey);
        }
        
        try {
            // Normalize path (ensure it starts with /)
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            
            ImageIcon originalIcon = new ImageIcon(getClass().getResource(path));
            Image scaledImage = originalIcon.getImage()
                    .getScaledInstance(width, height, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(scaledImage);
            
            imageCache.put(cacheKey, resizedIcon);
            LOGGER.info("Loaded resized image: " + cacheKey);
            return resizedIcon;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load resized image: " + path, e);
            return null;
        }
    }
    
    /**
     * Load and cache a font
     */
    public Font getFont(String path, float size) {
        String cacheKey = path + "_" + size;
        
        if (fontCache.containsKey(cacheKey)) {
            return fontCache.get(cacheKey);
        }
        
        try {
            // Normalize path (ensure it starts with /)
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            
            Font font = Font.createFont(Font.TRUETYPE_FONT, 
                    getClass().getResourceAsStream(path));
            Font derivedFont = font.deriveFont(size);
            
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            fontCache.put(cacheKey, derivedFont);
            LOGGER.info("Loaded font: " + cacheKey);
            return derivedFont;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load font: " + path, e);
            // Return a fallback font
            return new Font("Arial", Font.PLAIN, (int) size);
        }
    }
    
    /**
     * Clear all resource caches
     */
    public void clearCaches() {
        imageCache.clear();
        fontCache.clear();
        LOGGER.info("Resource caches cleared");
    }
}