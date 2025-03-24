package org.alex_hashtag;

import javax.swing.*;
import java.awt.*;

public class SignUpLoginForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;

    public SignUpLoginForm() {
        setTitle("Login / Sign Up");
        setSize(300, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        var panel = new JPanel(new GridLayout(4, 1));
        emailField = new JTextField();
        passwordField = new JPasswordField();

        var loginBtn = new JButton("Login");
        var signupBtn = new JButton("Sign Up");

        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        add(panel, BorderLayout.CENTER);

        var btnPanel = new JPanel();
        btnPanel.add(loginBtn);
        btnPanel.add(signupBtn);
        add(btnPanel, BorderLayout.SOUTH);

        signupBtn.addActionListener(e -> signUp());
        loginBtn.addActionListener(e -> login());

        setVisible(true);
    }

    private void signUp() {
        var email = emailField.getText();
        var password = new String(passwordField.getPassword());

        if (!email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format");
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters");
            return;
        }

        try (var conn = Database.getConnection();
             var check = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {

            check.setString(1, email);
            var rs = check.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Email already registered.");
                return;
            }

            // Ask for admin code
            String role = "user";
            int choice = JOptionPane.showConfirmDialog(this, "Do you want to register as admin?", "Admin Signup", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                String code = JOptionPane.showInputDialog(this, "Enter admin code:");
                if ("1234".equals(code)) {
                    role = "admin";
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid code. You’ll be registered as a user.");
                }
            }

            String salt = Database.generateSalt();
            String hash = Database.hash(password, salt);

            try (var insert = conn.prepareStatement("INSERT INTO users(username, salt, hash, role) VALUES (?, ?, ?, ?)")) {
                insert.setString(1, email);
                insert.setString(2, salt);
                insert.setString(3, hash);
                insert.setString(4, role);
                insert.executeUpdate();
                UserCache.loadUsers();
                JOptionPane.showMessageDialog(this, "Registered successfully as " + role);

                // ✅ Send validation email
                EmailSender.sendConfirmationEmail(email);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void login() {
        var email = emailField.getText();
        var password = new String(passwordField.getPassword());

        UserCache.loadUsers();
        var user = UserCache.cache.get(email);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "User not found.");
            return;
        }

        var expectedHash = Database.hash(password, user.salt);
        if (!expectedHash.equals(user.hash)) {
            JOptionPane.showMessageDialog(this, "Incorrect password.");
            return;
        }

        dispose();
        new WelcomeForm(user);
    }
}
