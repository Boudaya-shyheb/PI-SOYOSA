package com.example.messageservice.service;

import com.example.messageservice.dto.MessageDTO;
import com.example.messageservice.entities.Conversation;
import com.example.messageservice.entities.Message;
import com.example.messageservice.entities.MessageType;
import com.example.messageservice.exception.ConversationNotFoundException;
import com.example.messageservice.exception.MessageNotFoundException;
import com.example.messageservice.exception.UnauthorizedException;
import com.example.messageservice.repostories.ConversationRepository;
import com.example.messageservice.repostories.MessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final com.example.messageservice.repostories.ConversationMemberRepository conversationMemberRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    // GET MESSAGES

    @Transactional(readOnly = true)
    public List<MessageDTO> getConversationMessages(Long conversationId) {

        log.debug("Fetching messages for conversation {}", conversationId);

        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        return messageRepository.findByConversationId(conversationId)
                .stream()
                .map(this::toMessageDTO)
                .toList();
    }

    // SEND MESSAGE

    public MessageDTO sendMessage(
            Long conversationId,
            Long userId,
            String username,
            String content
    ) {

        System.out.println("CHECK conversationId = " + conversationId);
        System.out.println("CHECK userId = " + userId);
        conversationMemberRepository.findAllMembers(conversationId)
                .forEach(m -> System.out.println("MEMBER ID = " + m.getUserId()));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
        Boolean isMember =
                conversationMemberRepository.existsByConversation_IdAndUserId(conversationId, userId);

        System.out.println("IS MEMBER = " + isMember);

        if (!Boolean.TRUE.equals(isMember)) {
            throw new UnauthorizedException("User is not member of conversation");
        }

        Message message = Message.builder()
                .content(content)
                .senderId(userId)
                .senderUsername(username)
                .conversation(conversation)
                .messageType(MessageType.TEXT)
                .isDeleted(false)
                .build();

        message = messageRepository.save(message);

        log.info("Message sent {}", message.getId());

        MessageDTO dto = toMessageDTO(message);
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, dto);

        return dto;
    }

    // EDIT MESSAGE

    public MessageDTO editMessage(Long messageId, String username, String newContent) {

        log.debug("Editing message {} by user {}", messageId, username);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        Long conversationId = message.getConversation().getId();
        Long userId = conversationMemberRepository
                .findByConversation_IdAndUsername(conversationId, username)
                .orElseThrow(() -> new RuntimeException("User not in conversation"))
                .getUserId();

        if (!message.getSenderId().equals(userId)) {
            throw new UnauthorizedException("Only sender can edit message");
        }

        if (Boolean.TRUE.equals(message.getIsDeleted())) {
            throw new UnauthorizedException("Cannot edit deleted message");
        }

        message.setContent(newContent);

        message = messageRepository.save(message);

        MessageDTO dto = toMessageDTO(message);
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, dto);

        return dto;
    }

    // DELETE MESSAGE

    public void deleteMessage(Long messageId, String username) {

        log.debug("Deleting message {} by user {}", messageId, username);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        Long conversationId = message.getConversation().getId();
        Long userId = conversationMemberRepository
                .findByConversation_IdAndUsername(conversationId, username)
                .orElseThrow(() -> new RuntimeException("User not in conversation"))
                .getUserId();

        if (!message.getSenderId().equals(userId)) {
            throw new UnauthorizedException("Only sender can delete message");
        }

        message.setIsDeleted(true);

        message = messageRepository.save(message);

        log.info("Message deleted {}", messageId);

        MessageDTO dto = toMessageDTO(message);
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, dto);
    }



    // MAPPER

    private MessageDTO toMessageDTO(Message message) {

        return MessageDTO.builder()
                .id(message.getId())
                .content(message.getContent())
                .dateSent(message.getCreatedAt())
                .messageType(message.getMessageType().name())
                .senderId(message.getSenderId())
                .senderUsername(message.getSenderUsername())
                .messageRoomId(message.getConversation().getId())
                .updatedAt(message.getUpdatedAt())
                .isDeleted(message.getIsDeleted())
                .build();
    }
}