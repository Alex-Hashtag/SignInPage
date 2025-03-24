package org.alex_hashtag;

import javax.swing.*;

public class WelcomeForm extends JFrame {
    public WelcomeForm(User user) {
        setTitle("Welcome " + user.username);
        setSize(300, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        var label = new JLabel("Welcome, " + user.username + " (" + user.role + ")");
        var adminBtn = new JButton("Open Admin Panel");

        adminBtn.addActionListener(e -> {
            if (user.isAdmin()) {
                new AdminForm();
            } else {
                JOptionPane.showMessageDialog(this, "You are not an admin.");
            }
        });

        var panel = new JPanel();
        panel.add(label);
        panel.add(adminBtn);
        add(panel);
        setVisible(true);
    }
}
