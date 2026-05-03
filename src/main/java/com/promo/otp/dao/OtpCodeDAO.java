package com.promo.otp.dao;

import com.promo.otp.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

public class OtpCodeDAO {
    private static final Logger logger = LoggerFactory.getLogger(OtpCodeDAO.class);

    public static class OtpCode {
        private int id;
        private int userId;
        private String operationId;
        private String code;
        private String status;
        private String channel;
        private Timestamp createdAt;
        private Timestamp expiresAt;

        public OtpCode() {}

        public OtpCode(int userId, String operationId, String code, String channel, Timestamp expiresAt) {
            this.userId = userId;
            this.operationId = operationId;
            this.code = code;
            this.status = "ACTIVE";
            this.channel = channel;
            this.expiresAt = expiresAt;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getOperationId() { return operationId; }
        public void setOperationId(String operationId) { this.operationId = operationId; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }

        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

        public Timestamp getExpiresAt() { return expiresAt; }
        public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }
    }

    public boolean save(OtpCode otpCode) {
        String sql = "INSERT INTO otp_codes (user_id, operation_id, code, status, channel, expires_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, otpCode.getUserId());
            stmt.setString(2, otpCode.getOperationId());
            stmt.setString(3, otpCode.getCode());
            stmt.setString(4, otpCode.getStatus());
            stmt.setString(5, otpCode.getChannel());
            stmt.setTimestamp(6, otpCode.getExpiresAt());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    otpCode.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error saving OTP code", e);
        }
        return false;
    }

    public Optional<OtpCode> findActiveByOperation(String operationId, int userId) {
        String sql = "SELECT * FROM otp_codes WHERE operation_id = ? AND user_id = ? AND status = 'ACTIVE' AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, operationId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToOtpCode(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding active OTP code", e);
        }
        return Optional.empty();
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE otp_codes SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating OTP status", e);
        }
        return false;
    }

    public int markExpiredCodes() {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND expires_at < NOW()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            return stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error marking expired codes", e);
        }
        return 0;
    }

    public boolean deactivateAllCodesForOperation(String operationId, int userId) {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' WHERE operation_id = ? AND user_id = ? AND status = 'ACTIVE'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, operationId);
            stmt.setInt(2, userId);
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                logger.info("Deactivated {} old codes for operation: {}", updated, operationId);
            }
            return true;
        } catch (SQLException e) {
            logger.error("Error deactivating old codes", e);
            return false;
        }
    }

    private OtpCode mapResultSetToOtpCode(ResultSet rs) throws SQLException {
        OtpCode otpCode = new OtpCode();
        otpCode.setId(rs.getInt("id"));
        otpCode.setUserId(rs.getInt("user_id"));
        otpCode.setOperationId(rs.getString("operation_id"));
        otpCode.setCode(rs.getString("code"));
        otpCode.setStatus(rs.getString("status"));
        otpCode.setChannel(rs.getString("channel"));
        otpCode.setCreatedAt(rs.getTimestamp("created_at"));
        otpCode.setExpiresAt(rs.getTimestamp("expires_at"));
        return otpCode;
    }
}