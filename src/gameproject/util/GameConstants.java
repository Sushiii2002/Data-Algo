package gameproject.util;

import java.awt.Color;
import java.awt.Dimension;

/**
 * Constants for the game
 */
public class GameConstants {
    // Game window
    public static final String GAME_TITLE = "SmartSortStory";
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 768;
    
    // Resources
    public static final String FONT_PATH = "/gameproject/resources/PixelifySans.ttf";
    public static final String BG_IMAGE_PATH = "/gameproject/resources/levelBG.png";
    public static final String ARROW_BACK_PATH = "/gameproject/resources/arrowBack.png";
    public static final String FILLED_STAR_PATH = "/gameproject/resources/filledStar.png";
    public static final String EMPTY_STAR_PATH = "/gameproject/resources/emptyStar.png";
    
    // Level resources
    public static final String[] LEVEL_IMAGE_PATHS = {
        "/gameproject/resources/level1.png",
        "/gameproject/resources/level2.png",
        "/gameproject/resources/level3.png"
    };
    
    // UI dimensions
    public static final int LEVEL_BOX_SIZE = 150;
    public static final int STAR_SIZE = 32;
    public static final int LEVEL_SPACING = 50;
    public static final int TOP_PADDING = 80;
    public static final int ARROW_SIZE = 40;
    
    // Colors
    public static final Color PRIMARY_COLOR = new Color(75, 0, 130);
    public static final Color ACCENT_COLOR = new Color(255, 215, 0);
    public static final Color TEXT_COLOR = Color.WHITE;
    
    // Fonts
    public static final float TITLE_FONT_SIZE = 36f;
    public static final float SUBTITLE_FONT_SIZE = 18f;
    public static final float BUTTON_FONT_SIZE = 16f;
    
    // Button dimensions
    public static final Dimension MENU_BUTTON_SIZE = new Dimension(200, 50);
    
    // Animation timing
    public static final int SORT_ANIMATION_DELAY = 500; // ms
    
    // Difficulty levels
    public static final String[] DIFFICULTY_LEVELS = {
        "Beginner", "Intermediate", "Advanced"
    };
    
    // Max stars per level
    public static final int MAX_STARS = 3;
    
    private GameConstants() {
        // Private constructor to prevent instantiation
    }
}