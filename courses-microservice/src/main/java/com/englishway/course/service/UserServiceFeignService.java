package com.englishway.course.service;

import com.englishway.course.client.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for communicating with User Service via Feign client.
 * Handles token validation and user information retrieval.
 */
@Slf4j
@Service
public class UserServiceFeignService {

    private final UserServiceClient userServiceClient;

    public UserServiceFeignService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    /**
     * Validate a JWT token by delegating to user service
     * @param authHeader Authorization header (Bearer <token>)
     * @return True if token is valid, false otherwise
     */
    public boolean validateToken(String authHeader) {
        try {
            UserServiceClient.TokenValidationDto response = userServiceClient.validateToken(authHeader);
            return response != null && response.isValid();
        } catch (Exception e) {
            log.error("Error validating token with user service: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get user role from a JWT token
     * @param authHeader Authorization header (Bearer <token>)
     * @return User role, or null if token is invalid
     */
    public String getUserRole(String authHeader) {
        try {
            return userServiceClient.getUserRole(authHeader);
        } catch (Exception e) {
            log.error("Error retrieving user role from user service: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get user ID from a JWT token
     * @param authHeader Authorization header (Bearer <token>)
     * @return User ID, or null if token is invalid
     */
    public String getUserId(String authHeader) {
        try {
            return userServiceClient.getUserId(authHeader);
        } catch (Exception e) {
            log.error("Error retrieving user ID from user service: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validate token and get complete validation response
     * @param authHeader Authorization header (Bearer <token>)
     * @return Token validation response with all details
     */
    public UserServiceClient.TokenValidationDto getTokenValidation(String authHeader) {
        try {
            return userServiceClient.validateToken(authHeader);
        } catch (Exception e) {
            log.error("Error getting token validation from user service: {}", e.getMessage());
            return new UserServiceClient.TokenValidationDto(false, null, null, null);
        }
    }

    /**
     * Get user information by user ID
     * @param userId User ID (username or identifier)
     * @return User information with name, email, etc
     */
    public UserServiceClient.UserInfoDto getUserInfo(String userId) {
        try {
            return userServiceClient.getUserInfo(userId);
        } catch (Exception e) {
            log.error("Error retrieving user info for user {}: {}", userId, e.getMessage());
            // Return a basic DTO with just the userId as fallback
            UserServiceClient.UserInfoDto fallback = new UserServiceClient.UserInfoDto();
            fallback.setUserId(userId);
            fallback.setUsername(userId);
            return fallback;
        }
    }
}
