package ru.mephi.sirnya.dao;

import ru.mephi.sirnya.config.DatabaseConnector;
import ru.mephi.sirnya.model.OtpCode;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class OtpCodeDao {
    private final DatabaseConnector connector;

    public OtpCodeDao(DatabaseConnector connector) {
        this.connector = connector;
    }

    public void save(OtpCode otpCode) throws SQLException {
        String sql = "INSERT INTO otp_codes (user_id, operation_id, code, status, created_at, expires_at) VALUES (?, ?, ?, ?::otp_status, ?, ?)";
        String sqlFixed = "INSERT INTO otp_codes (user_id, operation_id, code, status, created_at, expires_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlFixed)) {
            ps.setInt(1, otpCode.getUserId());
            ps.setString(2, otpCode.getOperationId());
            ps.setString(3, otpCode.getCode());
            ps.setString(4, otpCode.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(otpCode.getCreatedAt() != null ? otpCode.getCreatedAt() : LocalDateTime.now()));
            ps.setTimestamp(6, Timestamp.valueOf(otpCode.getExpiresAt()));
            ps.executeUpdate();
        }
    }

    public Optional<OtpCode> findActiveByUserAndOperation(int userId, String operationId) throws SQLException {
        String sql = "SELECT * FROM otp_codes WHERE user_id = ? AND operation_id = ? AND status = 'ACTIVE' ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, operationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapCode(rs));
            }
        }
        return Optional.empty();
    }

    public void updateStatus(int codeId, String status) throws SQLException {
        String sql = "UPDATE otp_codes SET status = ? WHERE id = ?";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, codeId);
            ps.executeUpdate();
        }
    }

    public void expireOldCodes() throws SQLException {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND expires_at < CURRENT_TIMESTAMP";
        try (Connection conn = connector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private OtpCode mapCode(ResultSet rs) throws SQLException {
        OtpCode code = new OtpCode();
        code.setId(rs.getInt("id"));
        code.setUserId(rs.getInt("user_id"));
        code.setOperationId(rs.getString("operation_id"));
        code.setCode(rs.getString("code"));
        code.setStatus(rs.getString("status"));
        code.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        code.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        return code;
    }
}