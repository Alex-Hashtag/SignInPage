package org.alex_hashtag;

import javax.swing.*;
import java.awt.*;

public class LoginForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private static final Color BACKGROUND_COLOR = new Color(25, 25, 35);
    private static final Color FOREGROUND_COLOR = new Color(200, 200, 220);
    private static final Color FIELD_BACKGROUND = new Color(45, 45, 55);
    private static final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 14);

    public LoginForm() {
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel with custom background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, BACKGROUND_COLOR,
                    0, getHeight(), new Color(45, 45, 65)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        JLabel titleLabel = new JLabel("Welcome Back", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(FOREGROUND_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        formPanel.add(titleLabel, gbc);

        // Email field
        JLabel emailLabel = createLabel("Email:");
        emailField = createTextField();
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        // Password field
        JLabel passwordLabel = createLabel("Password:");
        passwordField = new JPasswordField();
        styleField(passwordField);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        JButton loginBtn = createButton("Login");
        JButton signupBtn = createButton("Sign Up");
        buttonPanel.add(loginBtn);
        buttonPanel.add(signupBtn);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        formPanel.add(buttonPanel, gbc);

        loginBtn.addActionListener(e -> login());
        signupBtn.addActionListener(e -> {
            dispose();
            new SignUpForm();
        });

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MAIN_FONT);
        label.setForeground(FOREGROUND_COLOR);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    private void styleField(JTextField field) {
        field.setFont(MAIN_FONT);
        field.setForeground(FOREGROUND_COLOR);
        field.setBackground(FIELD_BACKGROUND);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 70)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setCaretColor(FOREGROUND_COLOR);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(MAIN_FONT);
        button.setForeground(FOREGROUND_COLOR);
        button.setBackground(new Color(65, 105, 225));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void login() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isBlank() || password.isBlank()) {
            showError("Please fill in all fields.");
            return;
        }

        UserCache.loadUsers();
        User user = UserCache.cache.get(email);

        if (user == null) {
            showError("❌ User not found.");
            return;
        }

        String expectedHash = Database.hash(password, user.salt);
        if (!expectedHash.equals(user.hash)) {
            showError("❌ Incorrect password.");
            return;
        }

        JOptionPane.showMessageDialog(this,
            "✅ Login successful!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
        dispose();

        // Open appropriate panel
        if ("admin".equalsIgnoreCase(user.role)) {
            new AdminForm(user);
        } else {
            new WelcomeForm(user);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}
