package com.ecommerce.tracking.config;

import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.ecommerce.tracking.service.OrderOwnershipService;

@Component
public class WebSocketSubscriptionInterceptor implements ChannelInterceptor {

    private static final String DEST_PREFIX = "/topic/orders/";

    private final OrderOwnershipService orderOwnershipService;

    public WebSocketSubscriptionInterceptor(OrderOwnershipService orderOwnershipService) {
        this.orderOwnershipService = orderOwnershipService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.SUBSCRIBE) {
            return message;
        }
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(DEST_PREFIX)) {
            return message;
        }
        Long orderId = parseOrderId(destination.substring(DEST_PREFIX.length()));
        if (orderId == null) {
            throw new MessageDeliveryException("Invalid order subscription destination");
        }
        Map<String, Object> session = accessor.getSessionAttributes();
        if (session == null) {
            throw new MessageDeliveryException("Missing session attributes");
        }
        String role = stringValue(session.get("role"));
        String userId = stringValue(session.get("userId"));
        String authHeader = stringValue(session.get("authHeader"));
        boolean allowed = orderOwnershipService.canSubscribe(authHeader, role, userId, orderId);
        if (!allowed) {
            throw new MessageDeliveryException("Not authorized to subscribe to this order");
        }
        return message;
    }

    private Long parseOrderId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
