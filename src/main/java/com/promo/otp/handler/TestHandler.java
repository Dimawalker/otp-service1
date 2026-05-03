package com.promo.otp.handler;

import com.promo.otp.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class TestHandler extends AuthenticatedHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String login = authenticate(exchange);
        if (login == null) return; // Ошибка уже отправлена

        String response = JsonUtil.success("Hello, " + login + "! You are authenticated!");
        sendJson(exchange, 200, response);
    }
}