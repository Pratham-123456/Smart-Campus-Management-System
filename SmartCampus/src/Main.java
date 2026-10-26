
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Smart Campus Management System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(700, 450);
            frame.setLocationRelativeTo(null);

            JLabel title = new JLabel("Smart Campus Management System", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 24));

            JLabel subtitle = new JLabel("Oracle Database | Java Swing", SwingConstants.CENTER);
            subtitle.setFont(new Font("Arial", Font.PLAIN, 15));

            JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
            panel.add(title);
            panel.add(subtitle);

            frame.add(panel);
            frame.setVisible(true);
        });
    }
}
