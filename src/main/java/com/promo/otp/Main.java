package com.promo.otp;

import com.promo.otp.handler.*;
import com.promo.otp.util.DatabaseConnection;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        logger.info("OTP Service Starting...");

        // Проверка подключения к БД
        try (var conn = DatabaseConnection.getConnection()) {
            logger.info("Successfully connected to PostgreSQL database!");
        }

        // Создаём HTTP сервер на порту 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Регистрируем обработчики
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/test", new TestHandler());
        server.createContext("/api/otp/generate", new GenerateOtpHandler());
        server.createContext("/api/otp/validate", new ValidateOtpHandler());

        server.setExecutor(null);
        server.start();

        logger.info("HTTP Server started on port 8080");
        logger.info("Available endpoints:");
        logger.info("  POST /api/auth/register - register new user");
        logger.info("  POST /api/auth/login - login and get JWT token");
        logger.info("  GET  /api/test - test authenticated endpoint");
        logger.info("  POST /api/otp/generate - generate OTP code for operation");
        logger.info("  POST /api/otp/validate - validate OTP code");
    }
}