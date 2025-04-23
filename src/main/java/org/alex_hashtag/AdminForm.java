package org.alex_hashtag;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;

import static org.alex_hashtag.MarkdownUtil.*;

public class AdminForm extends JFrame {
    private final User currentUser;
    private final DefaultTableModel model;
    private final JTable table;

    private JLabel imageLabel = new JLabel("No Image", SwingConstants.CENTER);
    private JEditorPane bioPreview = new JEditorPane();
    private RSyntaxTextArea bioEditor = new RSyntaxTextArea();
    private JComboBox<String> syntaxSelector;
    private CardLayout bioLayout = new CardLayout();
    private JPanel bioCardPanel;
    private String currentCard = "preview"; // NEW: track current card

    private File newImageFile = null;
    private int selectedUserId = -1;

    private final int pageSize = 10;
    private int currentPage = 0;
    private int totalUsers = 0;
    private Connection conn;

    public AdminForm(User currentUser) {
        this.currentUser = currentUser;
        setTitle("Admin Panel - " + currentUser.username);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"ID", "Email", "Role", "Registered"}, 0) {
            public boolean isCellEditable(int row, int col) {
                int id = (int) getValueAt(row, 0);
                return col != 0 && col != 3 && id != currentUser.id;
            }
        };

        table = new JTable(model);
        table.setRowHeight(24);

        try {
            conn = Database.getConnection();
            totalUsers = getUserCount();
        } catch (Exception e) {
            showError("Database error: " + e.getMessage());
            return;
        }

        loadPage();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showUserDetails();
        });

        JScrollPane tableScroll = new JScrollPane(table);
        add(tableScroll, BorderLayout.CENTER);

        // Right Panel
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBorder(BorderFactory.createTitledBorder("User Details"));

        imageLabel.setPreferredSize(new Dimension(150, 150));
        rightPanel.add(imageLabel, BorderLayout.NORTH);

        JButton uploadImageBtn = new JButton("Upload Image");
        uploadImageBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                newImageFile = chooser.getSelectedFile();
                try {
                    BufferedImage img = ImageIO.read(newImageFile);
                    Image scaled = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaled));
                    imageLabel.setText("");
                } catch (IOException ex) {
                    showError("Could not preview image.");
                }
            }
        });

        // Bio components
        bioPreview.setEditable(false);
        bioPreview.setContentType("text/html");

        bioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
        bioEditor.setCodeFoldingEnabled(true);
        JScrollPane previewScroll = new JScrollPane(bioPreview);
        RTextScrollPane editorScroll = new RTextScrollPane(bioEditor);

        bioCardPanel = new JPanel(bioLayout);
        bioCardPanel.add(previewScroll, "preview");
        bioCardPanel.add(editorScroll, "editor");

        syntaxSelector = new JComboBox<>(new String[]{"Markdown", "HTML", "Plain Text"});
        syntaxSelector.setVisible(false);
        syntaxSelector.addActionListener(e -> switchSyntax((String) syntaxSelector.getSelectedItem()));

        JButton editBioBtn = new JButton("✏ Edit Bio");
        editBioBtn.addActionListener(e -> {
            if ("preview".equals(currentCard)) {
                syntaxSelector.setVisible(true);
                bioLayout.show(bioCardPanel, "editor");
                currentCard = "editor";
                editBioBtn.setText("💾 Save Bio");
            } else {
                saveBio();
                syntaxSelector.setVisible(false);
                bioLayout.show(bioCardPanel, "preview");
                currentCard = "preview";
                editBioBtn.setText("✏ Edit Bio");
            }
        });

        JPanel bioControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bioControlPanel.add(editBioBtn);
        bioControlPanel.add(syntaxSelector);

        JPanel bioPanel = new JPanel(new BorderLayout());
        bioPanel.add(bioControlPanel, BorderLayout.NORTH);
        bioPanel.add(bioCardPanel, BorderLayout.CENTER);
        rightPanel.add(bioPanel, BorderLayout.CENTER);
        rightPanel.add(uploadImageBtn, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);

        // Bottom Panel
        JButton saveBtn = new JButton("💾 Save All Changes");
        saveBtn.addActionListener(e -> saveChanges());

        JButton prevBtn = new JButton("⬅");
        JButton nextBtn = new JButton("➡");

        JLabel pageLabel = new JLabel();
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(prevBtn);
        bottomPanel.add(pageLabel);
        bottomPanel.add(nextBtn);
        bottomPanel.add(saveBtn);

        prevBtn.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadPage();
            }
        });

        nextBtn.addActionListener(e -> {
            if ((currentPage + 1) * pageSize < totalUsers) {
                currentPage++;
                loadPage();
            }
        });

        add(bottomPanel, BorderLayout.SOUTH);
        updatePageLabel(pageLabel);

        setVisible(true);
    }

    private void saveBio() {
        try {
            String updated = bioEditor.getText();
            PreparedStatement stmt = conn.prepareStatement("UPDATE users SET profile_bio = ? WHERE id = ?");
            stmt.setString(1, updated);
            stmt.setInt(2, selectedUserId);
            stmt.executeUpdate();
            showMessage("✅ Bio updated.");
            updateBioPreview(updated); // Updated preview
        } catch (Exception e) {
            showError("Error saving bio: " + e.getMessage());
        }
    }

    private void updateBioPreview(String bio) {
        String syntax = ((String) syntaxSelector.getSelectedItem()).toLowerCase();
        switch (syntax) {
            case "markdown" -> bioPreview.setText("<html><body>" + renderMarkdownToHtml(bio) + "</body></html>");
            case "html" -> bioPreview.setText(bio);
            default -> bioPreview.setText("<pre>" + escapeHtml(bio) + "</pre>");
        }
    }

    private void switchSyntax(String type) {
        if (type == null) return;

        switch (type.toLowerCase()) {
            case "markdown" -> bioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
            case "html" -> bioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
            default -> bioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        }
    }

    private void showUserDetails() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        int modelRow = table.convertRowIndexToModel(row);
        selectedUserId = (int) model.getValueAt(modelRow, 0);

        try (PreparedStatement stmt = conn.prepareStatement("SELECT profile_image, profile_bio FROM users WHERE id = ?")) {
            stmt.setInt(1, selectedUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                byte[] imgBytes = rs.getBytes("profile_image");
                String bio = rs.getString("profile_bio");

                if (imgBytes != null) {
                    ImageIcon icon = new ImageIcon(imgBytes);
                    Image scaled = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaled));
                    imageLabel.setText("");
                } else {
                    imageLabel.setIcon(null);
                    imageLabel.setText("No Image");
                }

                bioEditor.setText(bio == null ? "" : bio);
                updateBioPreview(bio == null ? "" : bio);
                bioLayout.show(bioCardPanel, "preview");
                currentCard = "preview";
            }

        } catch (Exception e) {
            showError("Failed to load user: " + e.getMessage());
        }
    }

    private void saveChanges() {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET username = ?, role = ?, profile_image = ? WHERE id = ?")) {
            for (int i = 0; i < model.getRowCount(); i++) {
                int id = (int) model.getValueAt(i, 0);
                String email = model.getValueAt(i, 1).toString().trim();
                String role = model.getValueAt(i, 2).toString().trim().toLowerCase();

                if (!email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
                    showError("Invalid email at row " + (i + 1));
                    return;
                }

                if (!(role.equals("admin") || role.equals("user"))) {
                    showError("Invalid role at row " + (i + 1));
                    return;
                }

                if (id == currentUser.id && !role.equals(currentUser.role)) {
                    showError("You can't change your own role.");
                    return;
                }

                stmt.setString(1, email);
                stmt.setString(2, role);

                if (selectedUserId == id && newImageFile != null) {
                    stmt.setBinaryStream(3, new FileInputStream(newImageFile), newImageFile.length());
                } else {
                    stmt.setNull(3, Types.BLOB);
                }

                stmt.setInt(4, id);
                stmt.addBatch();
            }

            stmt.executeBatch();
            showMessage("✅ Changes saved.");
            loadPage();
        } catch (Exception e) {
            showError("Error saving: " + e.getMessage());
        }
    }

    private void loadPage() {
        model.setRowCount(0);
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id, username, role, registration_date FROM users ORDER BY id ASC LIMIT ? OFFSET ?")) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, currentPage * pageSize);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getTimestamp("registration_date")
                });
            }
        } catch (Exception e) {
            showError("Load error: " + e.getMessage());
        }
    }

    private int getUserCount() {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void updatePageLabel(JLabel label) {
        int totalPages = (int) Math.ceil(totalUsers / (double) pageSize);
        label.setText("Page " + (currentPage + 1) + " of " + totalPages);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "❌ Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}
