package com.ecommerce.tracking.service;

import com.ecommerce.tracking.client.OrderServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderOwnershipServiceTest {

    @Mock
    private OrderServiceClient orderServiceClient;

    @Test
    void canSubscribe_allowsAdmin() {
        OrderOwnershipService service = new OrderOwnershipService(orderServiceClient);

        boolean allowed = service.canSubscribe("Bearer token", "ADMIN", "7", 100L);

        assertThat(allowed).isTrue();
    }

    @Test
    void canSubscribe_allowsCustomerThatOwnsOrder() {
        OrderServiceClient.ApiResponse<OrderServiceClient.OrderOwnerDto> response =
            new OrderServiceClient.ApiResponse<>();
        OrderServiceClient.OrderOwnerDto owner = new OrderServiceClient.OrderOwnerDto();
        owner.setCustomerId(5L);
        response.setData(owner);

        when(orderServiceClient.getOrderOwner(anyString(), anyLong())).thenReturn(response);

        OrderOwnershipService service = new OrderOwnershipService(orderServiceClient);

        boolean allowed = service.canSubscribe("Bearer token", "CUSTOMER", "5", 100L);

        assertThat(allowed).isTrue();
    }

    @Test
    void canSubscribe_allowsCourierWhenAssigned() {
        OrderServiceClient.ApiResponse<OrderServiceClient.OrderCourierDto> response =
            new OrderServiceClient.ApiResponse<>();
        OrderServiceClient.OrderCourierDto courier = new OrderServiceClient.OrderCourierDto();
        courier.setCourierId(9L);
        response.setData(courier);

        when(orderServiceClient.getOrderCourier(anyString(), anyLong())).thenReturn(response);

        OrderOwnershipService service = new OrderOwnershipService(orderServiceClient);

        boolean allowed = service.canSubscribe("Bearer token", "", "9", 100L);

        assertThat(allowed).isTrue();
    }
}
