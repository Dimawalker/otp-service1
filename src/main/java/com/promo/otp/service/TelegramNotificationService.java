package com.promo.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TelegramNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final String botToken;
    private final boolean enabled;

    public TelegramNotificationService() {
        Properties config = loadConfig();
        this.botToken = config.getProperty("telegram.bot.token");

        if (botToken == null || botToken.isEmpty() || "your_bot_token_here".equals(botToken)) {
            logger.warn("Telegram bot token not configured. Telegram sending will be disabled.");
            this.enabled = false;
        } else {
            this.enabled = true;
            logger.info("Telegram notification service initialized");
        }
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (var input = TelegramNotificationService.class.getClassLoader()
                .getResourceAsStream("telegram.properties")) {
            if (input == null) {
                logger.warn("telegram.properties not found in classpath, Telegram disabled");
                return props;
            }
            props.load(input);
            logger.info("Telegram configuration loaded");
        } catch (Exception e) {
            logger.warn("Failed to load Telegram config", e);
        }
        return props;
    }

    public void sendCode(String chatId, String code) {
        if (!enabled) {
            logger.info("Telegram disabled. Would send code {} to chatId: {}", code, chatId);
            return;
        }

        logger.info("Sending Telegram message to chatId: {} with code: {}", chatId, code);

        HttpURLConnection conn = null;
        try {
            String message = "Your OTP code is: " + code;
            String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8.toString());

            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();

            // Читаем ответ для диагностики
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            if (responseCode == 200) {
                logger.info("Telegram message sent successfully to chatId: {}", chatId);
            } else {
                logger.error("Telegram API error: code={}, response={}", responseCode, response.toString());
            }

        } catch (Exception e) {
            logger.error("Failed to send Telegram message: {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}