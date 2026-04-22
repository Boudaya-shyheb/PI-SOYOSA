package com.esprit.microservice.trainingservice.services;

import com.esprit.microservice.trainingservice.entities.Notification;
import com.esprit.microservice.trainingservice.repositories.NotificationRepository;
import com.esprit.microservice.trainingservice.security.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationServiceImpl implements INotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Override
    public List<Notification> getMyNotifications(SecurityUser user) {
        return notificationRepository.findByStudentIdOrderByCreatedAtDesc(user.getId());
    }

    @Override
    public Map<String, Long> getUnreadCount(SecurityUser user) {
        long count = notificationRepository.countByStudentIdAndIsReadFalse(user.getId());
        Map<String, Long> result = new HashMap<>();
        result.put("unreadCount", count);
        return result;
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(SecurityUser user) {
        List<Notification> unread = notificationRepository.findByStudentIdOrderByCreatedAtDesc(user.getId())
                .stream().filter(n -> !n.isRead()).toList();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void createNotification(Long studentId, String message) {
        createNotification(studentId, message, "INFO", null);
    }

    @Override
    @Transactional
    public void createNotification(Long studentId, String message, String type, Long referenceId) {
        Notification n = new Notification();
        n.setStudentId(studentId);
        n.setMessage(message);
        n.setType(type);
        n.setReferenceId(referenceId);
        Notification saved = notificationRepository.save(n);

        // Push real-time notification via WebSocket
        messagingTemplate.convertAndSendToUser(
                studentId.toString(),
                "/queue/notifications",
                saved
        );
    }
}
