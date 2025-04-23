package org.alex_hashtag;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

/**
 * Main class of the application.
 */
public class Main {
    public static void main(String[] args) {
        // Set FlatLaf dark theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize FlatLaf: " + e.getMessage());
        }

        // Launch the welcome window
        SwingUtilities.invokeLater(() -> new WelcomeWindow());
    }
}
