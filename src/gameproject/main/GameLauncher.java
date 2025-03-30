package gameproject.main;

import gameproject.controller.GameController;

import javax.swing.*;

/**
 * Main class to launch the application
 */
public class GameLauncher {
    /**
     * Main method to start the application
     */
    public static void main(String[] args) {
        // Use the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start the application using SwingUtilities to ensure thread safety
        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            controller.startApplication();
        });
    }
}