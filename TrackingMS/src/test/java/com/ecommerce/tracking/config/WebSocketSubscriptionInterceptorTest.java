package com.ecommerce.tracking.config;

import com.ecommerce.tracking.service.OrderOwnershipService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketSubscriptionInterceptorTest {

    @Mock
    private OrderOwnershipService orderOwnershipService;

    @Test
    void preSend_allowsWhenAuthorized() {
        when(orderOwnershipService.canSubscribe(anyString(), anyString(), anyString(), anyLong()))
            .thenReturn(true);

        WebSocketSubscriptionInterceptor interceptor = new WebSocketSubscriptionInterceptor(orderOwnershipService);
        Message<byte[]> message = buildSubscribe("/topic/orders/42");

        Message<?> result = interceptor.preSend(message, null);

        assertThat(result).isSameAs(message);
    }

    @Test
    void preSend_blocksWhenUnauthorized() {
        when(orderOwnershipService.canSubscribe(anyString(), anyString(), anyString(), anyLong()))
            .thenReturn(false);

        WebSocketSubscriptionInterceptor interceptor = new WebSocketSubscriptionInterceptor(orderOwnershipService);
        Message<byte[]> message = buildSubscribe("/topic/orders/42");

        assertThatThrownBy(() -> interceptor.preSend(message, null))
            .isInstanceOf(MessageDeliveryException.class)
            .hasMessageContaining("Not authorized");
    }

    private Message<byte[]> buildSubscribe(String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("role", "CUSTOMER");
        sessionAttributes.put("userId", "5");
        sessionAttributes.put("authHeader", "Bearer token");
        accessor.setSessionAttributes(sessionAttributes);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
