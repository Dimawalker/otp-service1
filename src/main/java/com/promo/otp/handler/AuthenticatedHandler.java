package com.promo.otp.handler;

import com.promo.otp.util.JwtUtil;
import com.promo.otp.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public abstract class AuthenticatedHandler implements HttpHandler {

    protected String authenticate(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(exchange, 401, "Missing or invalid token");
            return null;
        }

        String token = authHeader.substring(7);
        String login = JwtUtil.validateTokenAndGetLogin(token);

        if (login == null) {
            sendError(exchange, 401, "Invalid or expired token");
            return null;
        }

        return login;
    }

    protected void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = JsonUtil.error(message);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }

    protected void sendSuccess(HttpExchange exchange, String message) throws IOException {
        String response = JsonUtil.success(message);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }

    protected void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, json.getBytes().length);
        exchange.getResponseBody().write(json.getBytes());
        exchange.getResponseBody().close();
    }
}