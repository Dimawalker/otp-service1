package com.promo.otp.service;

import com.promo.otp.dao.UserDAO;
import com.promo.otp.util.JwtUtil;
import com.promo.otp.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO = new UserDAO();

    public boolean register(String login, String password, String email, String phone) {
        try {
            if (userDAO.findByLogin(login).isPresent()) {
                logger.warn("User already exists: {}", login);
                return false;
            }

            if ("admin".equalsIgnoreCase(login)) {
                if (userDAO.adminExists()) {
                    logger.warn("Admin already exists, cannot create another admin");
                    return false;
                }
                String passwordHash = PasswordUtil.hashPassword(password);
                UserDAO.User admin = new UserDAO.User(login, passwordHash, "ADMIN");
                admin.setEmail(email);
                admin.setPhone(phone);
                return userDAO.create(admin);
            } else {
                String passwordHash = PasswordUtil.hashPassword(password);
                UserDAO.User user = new UserDAO.User(login, passwordHash, "USER");
                user.setEmail(email);
                user.setPhone(phone);
                return userDAO.create(user);
            }
        } catch (Exception e) {
            logger.error("Registration error for user: {}", login, e);
            return false;
        }
    }

    public String login(String login, String password) {
        var userOpt = userDAO.findByLogin(login);

        if (userOpt.isEmpty()) {
            logger.warn("Login failed: user not found - {}", login);
            return null;
        }

        UserDAO.User user = userOpt.get();

        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            logger.warn("Login failed: wrong password for - {}", login);
            return null;
        }

        String token = JwtUtil.generateToken(user.getLogin(), user.getRole());
        logger.info("User logged in successfully: {}", login);
        return token;
    }

    public boolean isAdmin(String token) {
        String role = JwtUtil.getRoleFromToken(token);
        return "ADMIN".equals(role);
    }

    public String getLoginFromToken(String token) {
        return JwtUtil.validateTokenAndGetLogin(token);
    }
}