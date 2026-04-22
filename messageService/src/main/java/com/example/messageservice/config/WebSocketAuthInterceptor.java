package com.example.messageservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.debug("WebSocket client connected");
            
            // Extract token from native headers or URI
            List<String> authorization = accessor.getNativeHeader("Authorization");
            String token = null;

            if (authorization != null && !authorization.isEmpty()) {
                String bearer = authorization.get(0);
                if (bearer.startsWith("Bearer ")) {
                    token = bearer.substring(7);
                }
            } 
            
            if (token == null) {
                // Check simple parameter
                String uri = accessor.getFirstNativeHeader("simpUser");
            }

            List<String> tokenHeaders = accessor.getNativeHeader("token");
            if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
                token = tokenHeaders.get(0);
            }

            if (token != null) {
                try {
                    String username = jwtService.extractUsername(token);
                    if (username != null) {
                        Principal principal = () -> username;
                        accessor.setUser(principal);
                    }
                } catch (Exception e) {
                    log.error("WebSocket JWT extraction failed: {}", e.getMessage());
                }
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            log.debug("User subscribing to: {}", destination);
        }

        return message;
    }
}
