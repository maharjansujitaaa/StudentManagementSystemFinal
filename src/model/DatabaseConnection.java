package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/student_management";
    private static final String USER     = "root";       // your MySQL username
    private static final String PASSWORD = "sheepplus@12"; // your MySQL password

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[DB] Connected to MySQL successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("[DB] MySQL Driver not found! Add the JAR to your project.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("[DB] Connection failed! Check your username/password.");
            e.printStackTrace();
        }
        return connection;
    }
}