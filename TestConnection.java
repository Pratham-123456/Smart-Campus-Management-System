
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.Console;
import java.util.Arrays;

public class TestConnection {

    public static void main(String[] args) {

        // Oracle Database connection details
        String url = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
        String username = "campus_app";

        // Read the database password securely
        Console console = System.console();

        if (console == null) {
            System.out.println(
                "Console unavailable. Run this program directly in a terminal."
            );
            return;
        }

        char[] passwordChars =
            console.readPassword("Enter database password: ");

        String password = new String(passwordChars);

        // Establish the JDBC connection
        try (
            Connection connection =
                DriverManager.getConnection(url, username, password)
        ) {

            System.out.println("\nConnected to Oracle successfully!");
            System.out.println("Database user: " + username);
            System.out.println(
                "Connection valid: " + connection.isValid(5)
            );

        } catch (SQLException e) {

            System.out.println("\nConnection failed.");
            System.out.println("Error code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());

        } finally {

            Arrays.fill(passwordChars, '\0');
            password = null;
        }
    }
}
