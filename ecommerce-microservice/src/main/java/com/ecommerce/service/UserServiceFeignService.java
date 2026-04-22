package com.ecommerce.service;

import com.ecommerce.client.UserServiceClient;
import org.springframework.stereotype.Service;

@Service
public class UserServiceFeignService {

    private final UserServiceClient userServiceClient;

    public UserServiceFeignService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public boolean validateToken(String authHeader) {
        try {
            UserServiceClient.TokenValidationDto response = userServiceClient.validateToken(authHeader);
            return response != null && response.isValid();
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserRole(String authHeader) {
        try {
            return userServiceClient.getUserRole(authHeader);
        } catch (Exception e) {
            return null;
        }
    }

    public String getUserId(String authHeader) {
        try {
            return userServiceClient.getUserId(authHeader);
        } catch (Exception e) {
            return null;
        }
    }

    public UserServiceClient.UserInfoDto getUserInfo(String userId) {
        try {
            return userServiceClient.getUserInfo(userId);
        } catch (Exception e) {
            UserServiceClient.UserInfoDto fallback = new UserServiceClient.UserInfoDto();
            fallback.setUserId(userId);
            fallback.setUsername(userId);
            return fallback;
        }
    }
}
