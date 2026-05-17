package ru.mephi.sirnya.dao;


import ru.mephi.sirnya.config.DatabaseConnector;
import ru.mephi.sirnya.model.OtpConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OtpConfigDao {
    private final DatabaseConnector connector;

    public OtpConfigDao(DatabaseConnector connector) {
        this.connector = connector;
    }

    public OtpConfig getConfig() throws SQLException {
        String sql = "SELECT code_length, lifetime_seconds FROM otp_config WHERE id = 1";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new OtpConfig(rs.getInt("code_length"), rs.getInt("lifetime_seconds"));
            }
        }
        throw new SQLException("OTP configuration not found");
    }

    public void updateConfig(OtpConfig config) throws SQLException {
        String sql = "UPDATE otp_config SET code_length = ?, lifetime_seconds = ?, updated_at = CURRENT_TIMESTAMP WHERE id = 1";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, config.getCodeLength());
            ps.setInt(2, config.getLifetimeSeconds());
            ps.executeUpdate();
        }
    }
}