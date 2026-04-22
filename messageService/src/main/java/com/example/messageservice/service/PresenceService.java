package com.example.messageservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService {

    private final SimpMessagingTemplate messagingTemplate;

    // Tracks connected users. ConcurrentHashMap ensures thread safety.
    // Username -> Set of session IDs (to handle multiple tabs)
    private final ConcurrentHashMap<String, Set<String>> connectedUsers = new ConcurrentHashMap<>();

    public void addSession(String username, String sessionId) {
        connectedUsers.compute(username, (user, sessions) -> {
            if (sessions == null) {
                sessions = ConcurrentHashMap.newKeySet();
                sessions.add(sessionId);
                log.info("User {} connected. First session: {}", username, sessionId);
            } else {
                sessions.add(sessionId);
            }
            return sessions;
        });
        broadcastOnlineUsers();
    }

    public void removeSession(String username, String sessionId) {
        connectedUsers.computeIfPresent(username, (user, sessions) -> {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                log.info("User {} disconnected completely.", username);
                return null;
            }
            return sessions;
        });
        broadcastOnlineUsers();
    }

    public List<String> getOnlineUsers() {
        return new ArrayList<>(connectedUsers.keySet());
    }

    public void broadcastOnlineUsers() {
        messagingTemplate.convertAndSend("/topic/presence", getOnlineUsers());
    }
}
