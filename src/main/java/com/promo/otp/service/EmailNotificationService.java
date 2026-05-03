package com.promo.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final Session session;
    private final String fromEmail;
    private final boolean enabled;

    public EmailNotificationService() {
        Properties config = loadConfig();
        this.fromEmail = config.getProperty("email.from");

        String username = config.getProperty("email.username");
        String password = config.getProperty("email.password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            logger.warn("Email not configured. Email sending will be disabled.");
            this.enabled = false;
            this.session = null;
            return;
        }

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", config.getProperty("mail.smtp.host"));
        mailProps.put("mail.smtp.port", config.getProperty("mail.smtp.port"));
        mailProps.put("mail.smtp.auth", config.getProperty("mail.smtp.auth"));
        mailProps.put("mail.smtp.starttls.enable", config.getProperty("mail.smtp.starttls.enable"));
        mailProps.put("mail.smtp.ssl.enable", config.getProperty("mail.smtp.ssl.enable", "true"));
        mailProps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        this.session = Session.getInstance(mailProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        this.enabled = true;
        logger.info("Email notification service initialized for {}", fromEmail);
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (var input = EmailNotificationService.class.getClassLoader()
                .getResourceAsStream("email.properties")) {
            if (input == null) {
                logger.warn("email.properties not found in classpath");
                return props;
            }
            props.load(input);
            logger.info("Email configuration loaded successfully");
        } catch (Exception e) {
            logger.warn("Failed to load email configuration", e);
        }
        return props;
    }

    public void sendCode(String toEmail, String code) {
        if (!enabled) {
            logger.info("Email disabled. Would send code {} to {}", code, toEmail);
            return;
        }

        logger.info("Sending OTP code to email: {}", toEmail);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Your OTP Code");
            message.setText("Your verification code is: " + code);

            Transport.send(message);
            logger.info("Email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            // Не бросаем исключение, чтобы сервер не падал
        }
    }
}