package com.example.messageservice.controller;

import com.example.messageservice.dto.MessageDTO;
import com.example.messageservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;
    private final com.example.messageservice.repostories.ConversationMemberRepository conversationMemberRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    // GET MESSAGES

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(
            @PathVariable Long conversationId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getDetails();

        // Check if user is member
        conversationMemberRepository.findByConversationAndUserId(conversationId, userId)
                .orElseThrow(() -> new com.example.messageservice.exception.UnauthorizedException("User is not member of conversation"));

        List<MessageDTO> messages =
                messageService.getConversationMessages(conversationId);

        return ResponseEntity.ok(messages);
    }

    // SEND MESSAGE

    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestBody Map<String, Object> request
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }

        Long authUserId = (Long) authentication.getDetails();
        String username = authentication.getName();
        Long conversationId = ((Number) request.get("conversationId")).longValue();

        Long userId = conversationMemberRepository
                .findByConversationAndUserId(conversationId, authUserId)
                .orElseThrow(() -> new RuntimeException("User not in conversation"))
                .getUserId();

        String content = (String) request.get("content");
        System.out.println("AUTH USERNAME = " + username);
        System.out.println("AUTH USER ID = " + userId);
        System.out.println("REQUEST conversationId = " + conversationId);
        MessageDTO message = messageService.sendMessage(
                conversationId,
                userId,
                username,
                content
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    // EDIT MESSAGE

    @PatchMapping("/messages/{messageId}")
    public ResponseEntity<MessageDTO> editMessage(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> request,
            Authentication authentication
    ) {

        String username = authentication.getName();
        String newContent = request.get("content");

        log.info("Edit message {} by user {}", messageId, username);

        MessageDTO message =
                messageService.editMessage(messageId, username, newContent);

        return ResponseEntity.ok(message);
    }

    // DELETE MESSAGE

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication
    ) {

        String username = authentication.getName();

        log.info("Delete message {} by user {}", messageId, username);

        messageService.deleteMessage(messageId, username);

        return ResponseEntity.noContent().build();
    }


    // TYPING WEBSOCKET

    @MessageMapping("/typing/{conversationId}")
    public void handleTyping(
            @DestinationVariable Long conversationId,
            java.security.Principal principal
    ) {
        if (principal == null) return;
        
        String username = principal.getName();
        java.util.Map<String, String> payload = new java.util.HashMap<>();
        payload.put("username", username);
        payload.put("typing", "true");

        messagingTemplate.convertAndSend("/topic/typing/" + conversationId, payload);
    }
}