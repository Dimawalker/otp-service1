package com.promo.otp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String success(String message) {
        ObjectNode node = mapper.createObjectNode();
        node.put("success", true);
        node.put("message", message);
        return node.toString();
    }

    public static String successWithData(String key, Object data) {
        ObjectNode node = mapper.createObjectNode();
        node.put("success", true);
        node.put(key, data.toString());
        return node.toString();
    }

    public static String error(String message) {
        ObjectNode node = mapper.createObjectNode();
        node.put("success", false);
        node.put("error", message);
        return node.toString();
    }

    public static String tokenResponse(String token) {
        ObjectNode node = mapper.createObjectNode();
        node.put("success", true);
        node.put("token", token);
        return node.toString();
    }
}