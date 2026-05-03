package com.promo.otp.service;

import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SubmitSmResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SmsNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);

    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsNotificationService() {
        Properties config = loadConfig();
        this.host = config.getProperty("smpp.host", "localhost");
        this.port = Integer.parseInt(config.getProperty("smpp.port", "2775"));
        this.systemId = config.getProperty("smpp.system_id", "smppclient1");
        this.password = config.getProperty("smpp.password", "password");
        this.systemType = config.getProperty("smpp.system_type", "OTP");
        this.sourceAddress = config.getProperty("smpp.source_addr", "OTPService");

        logger.info("SMS service initialized for {}:{}", host, port);
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (var input = SmsNotificationService.class.getClassLoader()
                .getResourceAsStream("sms.properties")) {
            if (input == null) {
                logger.warn("sms.properties not found, using defaults");
                return props;
            }
            props.load(input);
            logger.info("SMS configuration loaded");
        } catch (Exception e) {
            logger.warn("Failed to load SMS config", e);
        }
        return props;
    }

    public void sendCode(String phoneNumber, String code) {
        logger.info("Sending SMS to {} with code: {}", phoneNumber, code);

        SMPPSession session = new SMPPSession();

        try {
            BindParameter bindParameter = new BindParameter(
                    BindType.BIND_TX,
                    systemId,
                    password,
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress
            );

            session.connectAndBind(host, port, bindParameter);
            logger.info("Connected to SMPP server");

            String message = "Your OTP code is: " + code;

            SubmitSmResult result = session.submitShortMessage(
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    phoneNumber,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    message.getBytes(StandardCharsets.UTF_8)
            );

            String messageId = result.getMessageId();
            logger.info("SMS sent successfully! Message ID: {}", messageId);

        } catch (Exception e) {
            logger.error("Failed to send SMS: {}", e.getMessage());
        } finally {
            try {
                if (session != null && session.getSessionState().isBound()) {
                    session.unbindAndClose();
                }
            } catch (Exception e) {
                logger.warn("Error closing session", e);
            }
        }
    }
}