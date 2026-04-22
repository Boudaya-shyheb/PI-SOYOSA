package com.esprit.microservice.trainingservice.services;

import com.esprit.microservice.trainingservice.entities.Notification;
import com.esprit.microservice.trainingservice.repositories.NotificationRepository;
import com.esprit.microservice.trainingservice.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private SecurityUser student;
    private Notification notification;

    @BeforeEach
    void setUp() {
        student = SecurityUser.builder()
                .id(100L)
                .build();

        notification = new Notification();
        notification.setId(1L);
        notification.setStudentId(100L);
        notification.setMessage("Test message");
        notification.setRead(false);
    }

    @Test
    void getMyNotifications_ShouldReturnList() {
        when(notificationRepository.findByStudentIdOrderByCreatedAtDesc(100L))
            .thenReturn(Collections.singletonList(notification));

        List<Notification> result = notificationService.getMyNotifications(student);

        assertFalse(result.isEmpty());
        assertEquals("Test message", result.get(0).getMessage());
    }

    @Test
    void markAsRead_ShouldUpdateStatus() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void createNotification_ShouldSave() {
        notificationService.createNotification(100L, "New announcement");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
