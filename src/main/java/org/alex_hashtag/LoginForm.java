package org.alex_hashtag;

import javax.swing.*;
import java.awt.*;

public class LoginForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginForm() {
        setTitle("Login");
        setSize(350, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        emailField = new JTextField();
        passwordField = new JPasswordField();
        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Sign Up");

        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginBtn);
        panel.add(signupBtn);

        loginBtn.addActionListener(e -> login());
        signupBtn.addActionListener(e -> {
            dispose();
            new SignUpForm();
        });

        add(panel);
        setVisible(true);
    }

    private void login() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        UserCache.loadUsers();
        User user = UserCache.cache.get(email);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "❌ User not found.");
            return;
        }

        String expectedHash = Database.hash(password, user.salt);
        if (!expectedHash.equals(user.hash)) {
            JOptionPane.showMessageDialog(this, "❌ Incorrect password.");
            return;
        }

        JOptionPane.showMessageDialog(this, "✅ Login successful!");
        dispose();

        // Open appropriate panel
        if ("admin".equalsIgnoreCase(user.role)) {
            new AdminForm(user);
        } else {
            new WelcomeForm(user);
        }
    }
}
