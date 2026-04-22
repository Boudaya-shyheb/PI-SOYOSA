package com.ecommerce.tracking.controller;

import com.ecommerce.tracking.dto.LocationUpdate;
import com.ecommerce.tracking.service.LocationStore;
import com.ecommerce.tracking.service.OrderOwnershipService;
import com.ecommerce.tracking.service.UserServiceFeignService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private LocationStore locationStore;

    @Mock
    private UserServiceFeignService userServiceFeignService;

    @Mock
    private OrderOwnershipService orderOwnershipService;

    @Test
    void update_savesLocationAndPublishesWhenAuthorized() {
        TrackingController controller = new TrackingController(
            messagingTemplate,
            locationStore,
            userServiceFeignService,
            orderOwnershipService
        );

        LocationUpdate update = new LocationUpdate();
        update.setOrderId(12L);
        update.setLat(36.8);
        update.setLng(10.18);

        when(userServiceFeignService.validateToken("Bearer token")).thenReturn(true);
        when(userServiceFeignService.getUserRole("Bearer token")).thenReturn("COURIER");
        when(userServiceFeignService.getUserId("Bearer token")).thenReturn("9");
        when(orderOwnershipService.canCourierUpdate("Bearer token", "9", 12L)).thenReturn(true);

        ResponseEntity<Void> response = controller.update("Bearer token", update);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(locationStore).saveLatest(eq(12L), eq(update));
        verify(messagingTemplate).convertAndSend(eq("/topic/orders/12"), eq(update));
    }

    @Test
    void update_throwsUnauthorizedWhenMissingAuthHeader() {
        TrackingController controller = new TrackingController(
            messagingTemplate,
            locationStore,
            userServiceFeignService,
            orderOwnershipService
        );

        LocationUpdate update = new LocationUpdate();
        update.setOrderId(12L);

        assertThatThrownBy(() -> controller.update(null, update))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Authorization header required");
    }
}
