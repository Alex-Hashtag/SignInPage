package org.alex_hashtag;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AdminForm extends JFrame {
    public AdminForm() {
        setTitle("Admin Panel");
        setSize(700, 400);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        tabs.add("Admins", createTablePanel("admin"));
        tabs.add("Users", createTablePanel("user"));

        add(tabs);
        setVisible(true);
    }

    private JPanel createTablePanel(String roleFilter) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"id", "username", "role"}, 0);
        JTable table = new JTable(model);
        loadUsers(roleFilter, model);

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> saveToDatabase(table));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(saveBtn, BorderLayout.SOUTH);
        return panel;
    }

    private void loadUsers(String role, DefaultTableModel model) {
        try (var conn = Database.getConnection();
             var stmt = conn.prepareStatement("SELECT id, username, role FROM users WHERE role = ?")) {
            stmt.setString(1, role);
            var rs = stmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("username"));
                row.add(rs.getString("role"));
                model.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void saveToDatabase(JTable table) {
        try (var conn = Database.getConnection();
             var stmt = conn.prepareStatement("UPDATE users SET username = ?, role = ? WHERE id = ?")) {
            for (int i = 0; i < table.getRowCount(); i++) {
                stmt.setString(1, table.getValueAt(i, 1).toString());
                stmt.setString(2, table.getValueAt(i, 2).toString());
                stmt.setInt(3, (Integer) table.getValueAt(i, 0));
                stmt.addBatch();
            }
            stmt.executeBatch();
            JOptionPane.showMessageDialog(this, "Changes saved.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
