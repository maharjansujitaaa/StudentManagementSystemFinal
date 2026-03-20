package model;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserStore {

    private static final UserStore INSTANCE = new UserStore();

    public static UserStore getInstance() {
        return INSTANCE;
    }

    private UserStore() {}

    // Check if username already exists
    public boolean userExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true if found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Save new user to database
    public void registerUser(String fullName, String email,
                             String username, String password, String role) {
        String sql = "INSERT INTO users (full_name, email, username, password, role) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, username.trim().toLowerCase());
            ps.setString(4, password);
            ps.setString(5, role);
            ps.executeUpdate();
            System.out.println("[DB] User registered: " + username);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Check username + password + role against database
    public boolean authenticate(String username, String password, String role) {
        String sql = "SELECT id FROM users WHERE username = ? "
                   + "AND password = ? AND role = ?";
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username.trim().toLowerCase());
            ps.setString(2, password);
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();
            boolean found = rs.next();
            System.out.println("[DB] Auth result for " + username + ": " + found);
            return found;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}