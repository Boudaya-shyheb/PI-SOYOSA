package com.ecommerce.tracking.config;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.ecommerce.tracking.service.UserServiceFeignService;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final UserServiceFeignService userServiceFeignService;

    public WebSocketAuthHandshakeInterceptor(UserServiceFeignService userServiceFeignService) {
        this.userServiceFeignService = userServiceFeignService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String authHeader = resolveAuthHeader(request);
        if (authHeader == null) {
            return false;
        }
        if (!userServiceFeignService.validateToken(authHeader)) {
            return false;
        }
        String role = userServiceFeignService.getUserRole(authHeader);
        String userId = userServiceFeignService.getUserId(authHeader);
        attributes.put("role", role);
        attributes.put("userId", userId);
        attributes.put("authHeader", authHeader);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String resolveAuthHeader(ServerHttpRequest request) {
        if (request.getHeaders().containsKey("Authorization")) {
            return request.getHeaders().getFirst("Authorization");
        }
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String token = httpRequest.getParameter("token");
            if (token != null && !token.isBlank()) {
                if (token.startsWith("Bearer ")) {
                    return token;
                }
                return "Bearer " + token;
            }
        }
        return null;
    }
}
