
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.io.Console;

public class TestDatabase {
    public static void main(String[] args) {
        Console console = System.console();

        if (console == null) {
            System.out.println("Run this program directly in the terminal.");
            return;
        }

        char[] passwordChars = console.readPassword("Enter database password: ");

        try (Connection connection = DatabaseConnection.getConnection(new String(passwordChars))) {
            System.out.println("Connected to Oracle successfully!");
            System.out.println("Connection valid: " + connection.isValid(5));
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }
}
