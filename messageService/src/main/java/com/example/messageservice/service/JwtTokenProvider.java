package com.example.messageservice.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Base64;


@Component
@Slf4j
public class JwtTokenProvider {


    public Long extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            log.error("Missing Authorization header");
            throw new IllegalArgumentException("Authorization header is required");
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.error("Invalid Authorization header format");
            throw new IllegalArgumentException("Invalid Authorization header format");
        }

        String token = authHeader.substring(7);
        return extractUserIdFromToken(token);
    }


    private Long extractUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }

            String payload = parts[1];

            // Add padding
            String paddedPayload = payload;
            int padding = 4 - (payload.length() % 4);
            if (padding != 4) {
                paddedPayload = payload + "=".repeat(padding);
            }

            byte[] decodedBytes = Base64.getUrlDecoder().decode(paddedPayload);
            String jsonPayload = new String(decodedBytes);

            log.debug("JWT Payload: {}", jsonPayload);

            long userId = extractFromJson(jsonPayload, "userId");
            log.debug("Extracted userId: {}", userId);
            return userId;

        } catch (Exception e) {
            log.error("Error decoding JWT token", e);
            throw new IllegalArgumentException("Invalid JWT token");
        }
    }


    private long extractFromJson(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);

        if (startIndex == -1) {
            throw new IllegalArgumentException("Key '" + key + "' not found in JWT");
        }

        int valueStart = startIndex + searchKey.length();
        int valueEnd = json.indexOf(",", valueStart);
        if (valueEnd == -1) {
            valueEnd = json.indexOf("}", valueStart);
        }

        String valueStr = json.substring(valueStart, valueEnd).trim();

        try {
            return Long.parseLong(valueStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid userId in JWT: " + valueStr);
        }
    }
}
