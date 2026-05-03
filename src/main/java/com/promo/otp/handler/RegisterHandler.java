package com.promo.otp.handler;

import com.promo.otp.service.AuthService;
import com.promo.otp.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class RegisterHandler implements HttpHandler {
    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // Читаем тело запроса
        String body = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().collect(Collectors.joining());

        // Простой парсинг (для production лучше использовать JSON-библиотеку)
        String login = extractValue(body, "login");
        String password = extractValue(body, "password");
        String email = extractValue(body, "email");
        String phone = extractValue(body, "phone");

        boolean success = authService.register(login, password, email, phone);

        String response;
        int statusCode;
        if (success) {
            response = JsonUtil.success("User registered successfully");
            statusCode = 201;
        } else {
            response = JsonUtil.error("Registration failed");
            statusCode = 400;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
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