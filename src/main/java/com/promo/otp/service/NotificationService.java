package com.promo.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final String FILE_PATH = "otp_codes.log";

    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;
    private final TelegramNotificationService telegramService;

    public NotificationService() {
        this.emailService = new EmailNotificationService();
        this.smsService = new SmsNotificationService();
        this.telegramService = new TelegramNotificationService();
    }

    public void sendToFile(String login, String operationId, String code) {
        try {
            Path currentDir = Paths.get("").toAbsolutePath();
            Path filePath = currentDir.resolve(FILE_PATH);

            try (PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile(), true))) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                writer.println(String.format("[%s] User: %s, Operation: %s, Code: %s",
                        timestamp, login, operationId, code));
                writer.flush();
                logger.info("OTP code saved to file: {}", filePath);
            }
        } catch (IOException e) {
            logger.error("Failed to write OTP code to file", e);
        }
    }

    public void sendToEmail(String toEmail, String code) {
        emailService.sendCode(toEmail, code);
    }

    public void sendToSms(String phoneNumber, String code) {
        smsService.sendCode(phoneNumber, code);
    }

    public void sendToTelegram(String chatId, String code) {
        telegramService.sendCode(chatId, code);
    }
}