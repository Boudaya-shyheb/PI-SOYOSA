package com.example.messageservice.service;

import com.example.messageservice.dto.MessageDTO;
import com.example.messageservice.entities.Conversation;
import com.example.messageservice.entities.Message;
import com.example.messageservice.entities.MessageType;
import com.example.messageservice.exception.ConversationNotFoundException;
import com.example.messageservice.exception.UnauthorizedException;
import com.example.messageservice.repostories.ConversationMemberRepository;
import com.example.messageservice.repostories.ConversationRepository;
import com.example.messageservice.repostories.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMemberRepository conversationMemberRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageService messageService;

    private Conversation conversation;
    private Long conversationId = 1L;
    private Long userId = 100L;
    private String username = "testuser";

    @BeforeEach
    void setUp() {
        conversation = Conversation.builder()
                .id(conversationId)
                .name("Test Group")
                .isGroup(true)
                .build();
    }

    @Test
    void getConversationMessages_Success() {
        // Arrange
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        Message message = Message.builder()
                .id(1L)
                .content("Hello")
                .senderId(userId)
                .senderUsername(username)
                .conversation(conversation)
                .messageType(MessageType.TEXT)
                .isDeleted(false)
                .build();
        when(messageRepository.findByConversationId(conversationId)).thenReturn(List.of(message));

        // Act
        List<MessageDTO> result = messageService.getConversationMessages(conversationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0).getContent());
        verify(messageRepository).findByConversationId(conversationId);
    }

    @Test
    void getConversationMessages_NotFound() {
        // Arrange
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConversationNotFoundException.class, () -> 
            messageService.getConversationMessages(conversationId)
        );
    }

    @Test
    void sendMessage_Success() {
        // Arrange
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.existsByConversation_IdAndUserId(conversationId, userId)).thenReturn(true);
        
        Message savedMessage = Message.builder()
                .id(1L)
                .content("New Message")
                .senderId(userId)
                .senderUsername(username)
                .conversation(conversation)
                .messageType(MessageType.TEXT)
                .isDeleted(false)
                .build();
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // Act
        MessageDTO result = messageService.sendMessage(conversationId, userId, username, "New Message");

        // Assert
        assertNotNull(result);
        assertEquals("New Message", result.getContent());
        verify(messageRepository).save(any(Message.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/conversation/" + conversationId), any(MessageDTO.class));
    }

    @Test
    void sendMessage_Unauthorized() {
        // Arrange
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));
        when(conversationMemberRepository.existsByConversation_IdAndUserId(conversationId, userId)).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> 
            messageService.sendMessage(conversationId, userId, username, "Exploit Attempt")
        );
        verify(messageRepository, never()).save(any(Message.class));
    }
}
