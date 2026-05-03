package com.promo.otp.filter;

import com.promo.otp.util.JwtUtil;
import com.promo.otp.util.JsonUtil;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.List;

public class AuthFilter extends Filter {

    private static final List<String> PUBLIC_PATHS = List.of("/api/auth/register", "/api/auth/login");

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (PUBLIC_PATHS.contains(path)) {
            chain.doFilter(exchange);
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(exchange, "Missing or invalid token");
            return;
        }

        String token = authHeader.substring(7);
        String login = JwtUtil.validateTokenAndGetLogin(token);

        if (login == null) {
            sendUnauthorized(exchange, "Invalid or expired token");
            return;
        }

        // Добавляем заголовок с логином для передачи в обработчик
        exchange.getRequestHeaders().add("X-User-Login", login);

        chain.doFilter(exchange);
    }

    private void sendUnauthorized(HttpExchange exchange, String message) throws IOException {
        String response = JsonUtil.error(message);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(401, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }

    @Override
    public String description() {
        return "Authentication filter";
    }
}