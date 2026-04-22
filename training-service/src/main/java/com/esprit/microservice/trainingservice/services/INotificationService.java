package com.esprit.microservice.trainingservice.services;

import com.esprit.microservice.trainingservice.entities.Notification;
import com.esprit.microservice.trainingservice.security.SecurityUser;
import java.util.List;
import java.util.Map;

public interface INotificationService {
    List<Notification> getMyNotifications(SecurityUser user);
    Map<String, Long> getUnreadCount(SecurityUser user);
    void markAsRead(Long notificationId);
    void markAllAsRead(SecurityUser user);
    void createNotification(Long studentId, String message);
    void createNotification(Long studentId, String message, String type, Long referenceId);

    //Notification getNotificationById(Long id);
}
