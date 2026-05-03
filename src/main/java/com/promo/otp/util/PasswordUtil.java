package com.promo.otp.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Хеширование пароля
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // Проверка пароля
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}