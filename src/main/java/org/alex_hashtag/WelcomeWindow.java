package org.alex_hashtag;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;

public class WelcomeWindow extends JFrame {
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 400;
    private static final int ANIMATION_DURATION = 2000; // 2 seconds
    private float scale = 0.1f;
    private float alpha = 0.0f;
    private Timer animationTimer;
    private long startTime;
    private JPanel animationPanel;
    private BufferedImage logo;

    public WelcomeWindow() {
        setTitle("Welcome");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true); // Remove window decorations for a sleek look

        // Create a custom panel with gradient background
        animationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(25, 25, 35),
                    0, getHeight(), new Color(45, 45, 65)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Draw welcome text
                g2d.setFont(new Font("Arial", Font.BOLD, 32));
                g2d.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
                String welcomeText = "Welcome";
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(welcomeText)) / 2;
                int textY = getHeight() / 4;
                g2d.drawString(welcomeText, textX, textY);

                // Draw logo if available
                if (logo != null) {
                    int logoWidth = (int)(logo.getWidth() * scale);
                    int logoHeight = (int)(logo.getHeight() * scale);
                    int x = (getWidth() - logoWidth) / 2;
                    int y = (getHeight() - logoHeight) / 2;

                    AffineTransform transform = new AffineTransform();
                    transform.translate(x, y);
                    transform.scale(scale, scale);

                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g2d.drawImage(logo, transform, null);
                }
            }
        };
        add(animationPanel);

        // Load logo
        try {
            // Create a simple placeholder logo
            logo = createPlaceholderLogo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start animation
        startAnimation();
        this.setVisible(true);
    }

    private BufferedImage createPlaceholderLogo() {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw a simple geometric logo
        g2d.setColor(new Color(65, 105, 225)); // Royal Blue
        g2d.fillOval(25, 25, 150, 150);
        g2d.setColor(new Color(135, 206, 235)); // Sky Blue
        g2d.fillOval(50, 50, 100, 100);
        g2d.dispose();
        return img;
    }

    private void startAnimation() {
        startTime = System.currentTimeMillis();
        animationTimer = new Timer(16, new ActionListener() { // ~60 FPS
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1.0f, (float)elapsed / ANIMATION_DURATION);

                // Ease in-out function
                progress = progress < 0.5f
                    ? 2 * progress * progress
                    : (float) (1 - Math.pow(-2 * progress + 2, 2) / 2);

                scale = 0.1f + progress * 0.9f; // Scale from 0.1 to 1.0
                alpha = progress; // Fade in from 0 to 1

                animationPanel.repaint();

                if (elapsed >= ANIMATION_DURATION) {
                    animationTimer.stop();
                    // Wait a moment, then proceed to login form
                    Timer transitionTimer = new Timer(1000, e2 -> {
                        dispose();
                        SwingUtilities.invokeLater(() -> new LoginForm());
                    });
                    transitionTimer.setRepeats(false);
                    transitionTimer.start();
                }
            }
        });
        animationTimer.start();
    }
}
