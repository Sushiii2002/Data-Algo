package gameproject.main;

import gameproject.controller.GameController;

import javax.swing.*;

/**
 * Main entry point for the SmartSortStory game
 */
public class GameLauncher {
    public static void main(String[] args) {
        // Use the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            controller.displayFrame();
        });
    }
}