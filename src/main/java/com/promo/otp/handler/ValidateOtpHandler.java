package com.promo.otp.handler;

import com.promo.otp.service.OtpService;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ValidateOtpHandler extends AuthenticatedHandler {
    private static final Logger logger = LoggerFactory.getLogger(ValidateOtpHandler.class);
    private final OtpService otpService = new OtpService();

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

        logger.info("Validate OTP request body: {}", body);

        String operationId = extractValue(body, "operation_id");
        String code = extractValue(body, "code");

        logger.info("Extracted - operationId: {}, code: {}", operationId, code);

        if (operationId == null || code == null) {
            sendError(exchange, 400, "Missing operation_id or code");
            return;
        }

        boolean isValid = otpService.validateCode(login, operationId, code);

        if (isValid) {
            String response = "{\"success\":true,\"message\":\"OTP code is valid!\"}";
            sendJson(exchange, 200, response);
        } else {
            sendError(exchange, 400, "Invalid or expired OTP code");
        }
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