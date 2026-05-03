package com.promo.otp.handler;

import com.promo.otp.dao.UserDAO;
import com.promo.otp.service.NotificationService;
import com.promo.otp.service.OtpService;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class GenerateOtpHandler extends AuthenticatedHandler {
    private static final Logger logger = LoggerFactory.getLogger(GenerateOtpHandler.class);
    private final OtpService otpService = new OtpService();
    private final NotificationService notificationService = new NotificationService();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String login = authenticate(exchange);
        if (login == null) return;

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }

        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().collect(Collectors.joining());

        logger.info("Generate OTP request body: {}", body);

        String operationId = extractValue(body, "operation_id");
        String channel = extractValue(body, "channel");

        logger.info("Extracted - operationId: {}, channel: {}", operationId, channel);

        if (operationId == null || channel == null) {
            sendError(exchange, 400, "Missing operation_id or channel");
            return;
        }

        var otpCode = otpService.createOtpCode(login, operationId, channel);

        if (otpCode == null) {
            sendError(exchange, 500, "Failed to generate OTP code");
            return;
        }

        // Отправляем код в зависимости от канала
        switch (channel.toUpperCase()) {
            case "FILE":
                notificationService.sendToFile(login, operationId, otpCode.getCode());
                break;

            case "EMAIL":
                var userOpt = userDAO.findByLogin(login);
                if (userOpt.isPresent() && userOpt.get().getEmail() != null) {
                    notificationService.sendToEmail(userOpt.get().getEmail(), otpCode.getCode());
                    logger.info("OTP code sent to email: {}", userOpt.get().getEmail());
                } else {
                    logger.warn("User {} has no email configured", login);
                }
                break;

            case "SMS":
                var userOptSms = userDAO.findByLogin(login);
                if (userOptSms.isPresent() && userOptSms.get().getPhone() != null) {
                    notificationService.sendToSms(userOptSms.get().getPhone(), otpCode.getCode());
                    logger.info("OTP code sent to SMS: {}", userOptSms.get().getPhone());
                } else {
                    logger.warn("User {} has no phone configured", login);
                }
                break;

            case "TELEGRAM":
                var userOptTelegram = userDAO.findByLogin(login);
                if (userOptTelegram.isPresent() && userOptTelegram.get().getTelegramChatId() != null) {
                    notificationService.sendToTelegram(String.valueOf(userOptTelegram.get().getTelegramChatId()), otpCode.getCode());
                    logger.info("OTP code sent to Telegram chatId: {}", userOptTelegram.get().getTelegramChatId());
                } else {
                    logger.warn("User {} has no telegram chatId configured", login);
                }
                break;

            default:
                logger.warn("Unknown channel: {}", channel);
        }

        logger.info("Generated OTP code: {} for operation: {}", otpCode.getCode(), operationId);

        String response = "{\"success\":true,\"message\":\"OTP code generated and sent via " + channel + "\"}";
        sendJson(exchange, 201, response);
    }

    private String extractValue(String body, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = body.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        int end = body.indexOf("\"", start);
        if (end == -1) return null;
        return body.substring(start, end);
    }
}