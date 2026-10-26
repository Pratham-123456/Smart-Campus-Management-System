
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private final JTextField studentIdField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);

    public LoginFrame() {
        setTitle("Smart Campus Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 390);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 20));
        mainPanel.setBorder(new EmptyBorder(30, 35, 30, 35));
        mainPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Smart Campus", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(30, 70, 120));

        JLabel subtitle = new JLabel("Student Portal", SwingConstants.CENTER);
        subtitle.setFont(new Font("Arial", Font.PLAIN, 16));

        JPanel heading = new JPanel(new GridLayout(2, 1, 5, 5));
        heading.setBackground(Color.WHITE);
        heading.add(title);
        heading.add(subtitle);

        JPanel form = new JPanel(new GridLayout(4, 1, 8, 8));
        form.setBackground(Color.WHITE);

        JPanel idPanel = new JPanel(new BorderLayout(5, 5));
        idPanel.setBackground(Color.WHITE);
        idPanel.add(new JLabel("Student ID"), BorderLayout.NORTH);
        idPanel.add(studentIdField, BorderLayout.CENTER);

        JPanel emailPanel = new JPanel(new BorderLayout(5, 5));
        emailPanel.setBackground(Color.WHITE);
        emailPanel.add(new JLabel("College Email"), BorderLayout.NORTH);
        emailPanel.add(emailField, BorderLayout.CENTER);

        JButton loginButton = new JButton("Verify and Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(30, 70, 120));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> verifyStudent());

        JLabel note = new JLabel("Demonstration login: Student ID + email");
        note.setFont(new Font("Arial", Font.PLAIN, 11));
        note.setForeground(Color.GRAY);
        note.setHorizontalAlignment(SwingConstants.CENTER);

        form.add(idPanel);
        form.add(emailPanel);
        form.add(loginButton);
        form.add(note);

        mainPanel.add(heading, BorderLayout.NORTH);
        mainPanel.add(form, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void verifyStudent() {
        String studentIdText = studentIdField.getText().trim();
        String email = emailField.getText().trim();

        if (studentIdText.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both Student ID and email.", "Missing Details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = Integer.parseInt(studentIdText);

            String sql = "SELECT Name FROM STUDENT WHERE Student_ID = ? AND LOWER(Email) = LOWER(?)";

            try (Connection connection = DatabaseConnection.getConnection(readPassword());
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setInt(1, studentId);
                statement.setString(2, email);

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        String name = result.getString("Name");
                        JOptionPane.showMessageDialog(this, "Welcome, " + name + "!\nStudent record verified successfully.");
                    } else {
                        JOptionPane.showMessageDialog(this, "Student ID or email was not found.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Student ID must be a number.", "Invalid Student ID", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Configuration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String readPassword() {
        String password = System.getenv("CAMPUS_DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Set the CAMPUS_DB_PASSWORD environment variable in your terminal before launching the app.");
        }
        return password;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
