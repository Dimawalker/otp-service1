package com.promo.otp.service;

import com.promo.otp.dao.OtpCodeDAO;
import com.promo.otp.dao.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private final OtpCodeDAO otpCodeDAO = new OtpCodeDAO();
    private final UserDAO userDAO = new UserDAO();
    private final SecureRandom random = new SecureRandom();

    private int codeLength = 6;
    private int ttlSeconds = 300;

    public String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    public OtpCodeDAO.OtpCode createOtpCode(String login, String operationId, String channel) {
        logger.info("Creating OTP code for login: {}, operation: {}, channel: {}", login, operationId, channel);

        var userOpt = userDAO.findByLogin(login);
        if (userOpt.isEmpty()) {
            logger.warn("User not found: {}", login);
            return null;
        }

        UserDAO.User user = userOpt.get();
        logger.info("Found user: id={}, login={}", user.getId(), user.getLogin());

        otpCodeDAO.deactivateAllCodesForOperation(operationId, user.getId());

        String code = generateCode();
        Timestamp expiresAt = Timestamp.valueOf(LocalDateTime.now().plusSeconds(ttlSeconds));

        OtpCodeDAO.OtpCode otpCode = new OtpCodeDAO.OtpCode(
                user.getId(), operationId, code, channel, expiresAt
        );

        if (otpCodeDAO.save(otpCode)) {
            logger.info("OTP code saved successfully. ID: {}, Code: {}", otpCode.getId(), otpCode.getCode());
            return otpCode;
        }

        logger.error("Failed to save OTP code");
        return null;
    }

    public boolean validateCode(String login, String operationId, String inputCode) {
        logger.info("Validating code for login: {}, operationId: {}, inputCode: {}", login, operationId, inputCode);

        var userOpt = userDAO.findByLogin(login);
        if (userOpt.isEmpty()) {
            logger.warn("User not found: {}", login);
            return false;
        }

        UserDAO.User user = userOpt.get();
        logger.info("Found user: id={}, login={}", user.getId(), user.getLogin());

        var otpCodeOpt = otpCodeDAO.findActiveByOperation(operationId, user.getId());

        if (otpCodeOpt.isEmpty()) {
            logger.warn("No active OTP code found for operation: {}", operationId);
            return false;
        }

        OtpCodeDAO.OtpCode otpCode = otpCodeOpt.get();
        logger.info("Found OTP code: id={}, code={}, status={}", otpCode.getId(), otpCode.getCode(), otpCode.getStatus());

        if (otpCode.getCode().equals(inputCode)) {
            otpCodeDAO.updateStatus(otpCode.getId(), "USED");
            logger.info("OTP code validated successfully");
            return true;
        } else {
            logger.warn("Code mismatch! Stored: {}, Input: {}", otpCode.getCode(), inputCode);
            return false;
        }
    }

    public int expireOldCodes() {
        int count = otpCodeDAO.markExpiredCodes();
        if (count > 0) {
            logger.info("Marked {} OTP codes as EXPIRED", count);
        }
        return count;
    }
}