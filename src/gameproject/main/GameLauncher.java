package gameproject.main;

import gameproject.controller.GameController;
import java.io.File;

import javax.swing.*;

/**
 * Main class to launch the application
 */
public class GameLauncher {
    /**
     * Main method to start the application
     */
    public static void main(String[] args) {
        // Delete any existing save file at startup
        try {
            File saveFile = new File("smartsortstory_progress.dat");
            if (saveFile.exists()) {
                boolean deleted = saveFile.delete();
                if (deleted) {
                    System.out.println("Progress file deleted - starting with fresh game state");
                } else {
                    System.out.println("Warning: Could not delete progress file");
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling progress file: " + e.getMessage());
        }

        // Continue with normal startup
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            controller.startApplication();
        });
    }
}