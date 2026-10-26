
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
    private static final String USERNAME = "campus_app";

    public static Connection getConnection(String password) throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, password);
    }
}
