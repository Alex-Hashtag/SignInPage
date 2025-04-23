package org.alex_hashtag;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;

import static org.alex_hashtag.MarkdownUtil.renderMarkdownToHtml;

public class SignUpForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel imagePreview;
    private File selectedImageFile = null;

    private RSyntaxTextArea bioEditor;
    private JEditorPane bioPreview;
    private JComboBox<String> syntaxSelector;

    public SignUpForm() {
        setTitle("Sign Up");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        JButton uploadImageBtn = new JButton("Upload Profile Image");
        imagePreview = new JLabel("No image", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(150, 150));

        uploadImageBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedImageFile = chooser.getSelectedFile();
                ImageIcon icon = new ImageIcon(selectedImageFile.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                imagePreview.setIcon(new ImageIcon(img));
                imagePreview.setText("");
            }
        });

        leftPanel.add(new JLabel("Email:"));
        leftPanel.add(emailField);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(new JLabel("Password:"));
        leftPanel.add(passwordField);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(new JLabel("Confirm Password:"));
        leftPanel.add(confirmPasswordField);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(uploadImageBtn);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(imagePreview);

        JButton signupBtn = new JButton("Sign Up");
        signupBtn.addActionListener(e -> signUp());
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(signupBtn);

        // Right panel (bio editor + preview)
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Bio"));

        // Syntax selector
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.add(new JLabel("Syntax: "));
        syntaxSelector = new JComboBox<>(new String[]{"Markdown", "HTML", "Plain Text"});
        syntaxSelector.addActionListener(e -> updateSyntax());
        topBar.add(syntaxSelector);
        rightPanel.add(topBar, BorderLayout.NORTH);

        // Split view
        bioEditor = new RSyntaxTextArea(15, 40);
        bioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
        bioEditor.setCodeFoldingEnabled(true);
        RTextScrollPane scrollEditor = new RTextScrollPane(bioEditor);

        bioPreview = new JEditorPane();
        bioPreview.setEditable(false);
        bioPreview.setContentType("text/html");
        JScrollPane scrollPreview = new JScrollPane(bioPreview);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollEditor, scrollPreview);
        splitPane.setDividerLocation(400);
        rightPanel.add(splitPane, BorderLayout.CENTER);

        bioEditor.getDocument().addDocumentListener((SimpleDocumentListener) this::updatePreview);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void updateSyntax() {
        String selected = (String) syntaxSelector.getSelectedItem();
        if (selected == null) return;

        switch (selected.toLowerCase()) {
            case "html" -> {
                bioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
                bioPreview.setContentType("text/html");
            }
            case "markdown" -> {
                bioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
                bioPreview.setContentType("text/html"); // We’ll render MD as simple HTML preview
            }
            default -> {
                bioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
                bioPreview.setContentType("text/plain");
            }
        }

        updatePreview();
    }

    private void updatePreview() {
        String text = bioEditor.getText();
        if ("Markdown".equals(syntaxSelector.getSelectedItem())) {
            // Very basic markdown-to-HTML (real parser optional)
            bioPreview.setText("<html><body>" + renderMarkdownToHtml(text) + "</body></html>");

        } else {
            bioPreview.setText(text);
        }
    }

    private void signUp() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (!email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format");
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
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

            try (var insert = conn.prepareStatement(
                    "INSERT INTO users(username, salt, hash, role, profile_image, profile_bio) VALUES (?, ?, ?, ?, ?, ?)")) {

                insert.setString(1, email);
                insert.setString(2, salt);
                insert.setString(3, hash);
                insert.setString(4, role);

                if (selectedImageFile != null) {
                    insert.setBinaryStream(5, new FileInputStream(selectedImageFile), selectedImageFile.length());
                } else {
                    insert.setNull(5, java.sql.Types.BLOB);
                }

                String rawBio = bioEditor.getText();
                insert.setString(6, rawBio.isBlank() ? null : rawBio);

                insert.executeUpdate();
                UserCache.loadUsers();

                // Send confirmation email
                try {
                    EmailSender.sendConfirmationEmail(email);
                    System.out.println("✅ Email sent to " + email);
                } catch (Exception e) {
                    System.err.println("❌ Email sending failed: " + e.getMessage());
                    e.printStackTrace();
                }

                JOptionPane.showMessageDialog(this, "Successfully registered as " + role);
                var user = UserCache.cache.get(email);
                dispose();
                new WelcomeForm(user);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage());
        }
    }

}
