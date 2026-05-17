package ru.mephi.sirnya.dao;

import ru.mephi.sirnya.config.DatabaseConnector;
import ru.mephi.sirnya.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private final DatabaseConnector connector;

    public UserDao(DatabaseConnector connector) {
        this.connector = connector;
    }

    public Optional<User> findByLogin(String login) throws SQLException {
        String sql = "SELECT id, login, password_hash, role FROM users WHERE login = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapUser(rs));
            }
        }
        return Optional.empty();
    }

    public User save(User user) throws SQLException {
        String sql = "INSERT INTO users (login, password_hash, role) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt("id"));
                return user;
            }
        }
        throw new SQLException("Failed to insert user");
    }



    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setLogin(rs.getString("login"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        return user;
    }

    public boolean existsByRole(String role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public int countByRole(String role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<User> findAllExceptAdmins() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, login, password_hash, role FROM users WHERE role <> 'ADMIN'";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void deleteOtpCodesByUser(int userId) throws SQLException {
        String sql = "DELETE FROM otp_codes WHERE user_id = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

}