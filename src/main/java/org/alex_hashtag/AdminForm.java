package org.alex_hashtag;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminForm extends JFrame {
    private final User currentUser;
    private final DefaultTableModel model;
    private final JTable table;

    public AdminForm(User currentUser) {
        this.currentUser = currentUser;

        setTitle("Admin Panel - Logged in as: " + currentUser.username);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"ID", "Email", "Role", "Registered"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                int id = (int) getValueAt(row, 0);
                // Prevent editing ID or Registration Date
                if (column == 0 || column == 3) return false;
                // Prevent self-role change
                if (column == 2 && id == currentUser.id) return false;
                return true;
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);

        loadUsers();

        JButton saveBtn = new JButton("💾 Save Changes");
        saveBtn.addActionListener(e -> saveToDatabase());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(saveBtn);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void loadUsers() {
        model.setRowCount(0); // clear

        try (var conn = Database.getConnection();
             var stmt = conn.prepareStatement("SELECT id, username, role, registration_date FROM users");
             var rs = stmt.executeQuery()) {

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            while (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("username");
                String role = rs.getString("role");
                String date = rs.getTimestamp("registration_date").toLocalDateTime().format(fmt);

                model.addRow(new Object[]{id, email, role, date});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + e.getMessage());
        }
    }

    private void saveToDatabase() {
        List<Object[]> updates = new ArrayList<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            int id = (int) model.getValueAt(i, 0);
            String email = model.getValueAt(i, 1).toString().trim();
            String role = model.getValueAt(i, 2).toString().trim().toLowerCase();

            // Email validation
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                JOptionPane.showMessageDialog(this, "Invalid email format at row " + (i + 1));
                return;
            }

            // Role validation
            if (!role.equals("admin") && !role.equals("user")) {
                JOptionPane.showMessageDialog(this, "Invalid role at row " + (i + 1) + ". Must be 'admin' or 'user'");
                return;
            }

            updates.add(new Object[]{email, role, id});
        }

        try (var conn = Database.getConnection();
             var stmt = conn.prepareStatement("UPDATE users SET username = ?, role = ? WHERE id = ?")) {

            for (Object[] update : updates) {
                stmt.setString(1, (String) update[0]);
                stmt.setString(2, (String) update[1]);
                stmt.setInt(3, (int) update[2]);
                stmt.addBatch();
            }

            stmt.executeBatch();
            JOptionPane.showMessageDialog(this, "✅ Changes saved.");
            loadUsers(); // reload updated data

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save: " + e.getMessage());
        }
    }
}
