package com.example.messageservice.mapper;

import com.example.messageservice.dto.ConversationDTO;
import com.example.messageservice.dto.ConversationMemberDTO;
import com.example.messageservice.dto.MessageDTO;
import com.example.messageservice.entities.Conversation;
import com.example.messageservice.entities.ConversationMember;
import com.example.messageservice.entities.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConversationMapper {

    public ConversationDTO toDTO(Conversation conversation, Message lastMessage) {
        List<ConversationMemberDTO> memberDTOs = conversation.getMembers().stream()
                .map(this::toMemberDTO)
                .collect(Collectors.toList());

        return ConversationDTO.builder()
                .id(conversation.getId())
                .name(conversation.getName())
                .isGroup(conversation.getIsGroup())
                .createdAt(conversation.getCreatedAt())
                .createdById(conversation.getCreatedById())
                .createdByUsername(conversation.getCreatedByUsername())
                .members(memberDTOs)
                .lastMessage(lastMessage != null ? toMessageDTO(lastMessage) : null)
                .build();
    }

    private ConversationMemberDTO toMemberDTO(ConversationMember member) {
        return ConversationMemberDTO.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .username(member.getUsername())
                .isAdmin(member.getIsAdmin())
                .lastSeen(member.getLastSeen())
                .build();
    }
    public ConversationDTO toDTO(Conversation conversation) {
        return toDTO(conversation, null);
    }
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