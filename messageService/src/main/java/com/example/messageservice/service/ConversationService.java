package com.example.messageservice.service;

import com.example.messageservice.dto.ConversationDTO;
import com.example.messageservice.entities.Conversation;
import com.example.messageservice.entities.ConversationMember;
import com.example.messageservice.entities.Message;
import com.example.messageservice.exception.ConversationNotFoundException;
import com.example.messageservice.exception.UnauthorizedException;
import com.example.messageservice.mapper.ConversationMapper;
import com.example.messageservice.repostories.ConversationMemberRepository;
import com.example.messageservice.repostories.ConversationRepository;
import com.example.messageservice.repostories.MessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final ConversationMapper conversationMapper;

    // USER CONVERSATIONS

    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversationsByUserId(Long userId) {

        log.debug("Fetching conversations for userId {}", userId);

        return conversationRepository.findAllByUserId(userId)
                .stream()
                .map(c -> {
                    Optional<Message> last =
                            messageRepository.findFirstByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(c.getId());

                    return conversationMapper.toDTO(c, last.orElse(null));
                })
                .toList();
    }

    // GET SINGLE CONVERSATION

    @Transactional(readOnly = true)
    public ConversationDTO getConversation(Long conversationId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        Optional<Message> lastMessage =
                messageRepository.findFirstByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(conversationId);

        return conversationMapper.toDTO(conversation, lastMessage.orElse(null));
    }

    // PRIVATE CONVERSATION

    public ConversationDTO createPrivateConversation(Long userId1, Long userId2) {

        log.debug("Creating private conversation {} {}", userId1, userId2);

        Optional<Conversation> existing =
                conversationRepository.findPrivateConversationBetween(userId1, userId2);

        if (existing.isPresent()) {

            Optional<Message> last =
                    messageRepository.findFirstByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(existing.get().getId());

            return conversationMapper.toDTO(existing.get(), last.orElse(null));
        }

        Conversation conversation = Conversation.builder()
                .isGroup(false)
                .createdById(userId1)
                .createdByUsername("user-" + userId1)
                .build();

        conversation = conversationRepository.save(conversation);

        addMember(conversation, userId1, true);
        addMember(conversation, userId2, false);

        Optional<Message> last =
                messageRepository.findFirstByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(conversation.getId());

        return conversationMapper.toDTO(conversation, last.orElse(null));
    }

    // GROUP CONVERSATION

    public ConversationDTO createGroupConversation(
            Long creatorUserId,
            String groupName,
            List<Long> memberIds
    ) {
        Conversation conversation = Conversation.builder()
                .name(groupName)
                .isGroup(true)
                .createdById(creatorUserId)
                .createdByUsername("user-" + creatorUserId)
                .build();
        final Conversation savedConversation = conversationRepository.save(conversation);
        addMember(savedConversation, creatorUserId, true);
        if (memberIds != null) {
            memberIds.stream()
                    .filter(id -> id != null && !id.equals(creatorUserId))
                    .forEach(id -> addMember(savedConversation, id, false));
        }
        Optional<Message> last =
                messageRepository.findFirstByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(savedConversation.getId());
        return conversationMapper.toDTO(savedConversation, last.orElse(null));
    }

    // DELETE CONVERSATION

    public void deleteConversation(Long conversationId, Long userId) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        if (conversation.getIsGroup()) {

            Boolean admin =
                    memberRepository.isUserAdminOfConversation(conversationId, userId);

            if (!Boolean.TRUE.equals(admin)) {
                throw new UnauthorizedException("Only admins can delete group");
            }

        } else {

            Boolean member =
                    conversationRepository.isUserMemberOfConversation(conversationId, userId);

            if (!Boolean.TRUE.equals(member)) {
                throw new UnauthorizedException("Only members can delete");
            }
        }

        conversationRepository.delete(conversation);
    }

    // RENAME GROUP

    public ConversationDTO renameGroup(Long conversationId, Long userId, String newName) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));

        Boolean admin =
                memberRepository.isUserAdminOfConversation(conversationId, userId);

        if (!Boolean.TRUE.equals(admin)) {
            throw new UnauthorizedException("Only admins can rename group");
        }

        conversation.setName(newName);

        conversationRepository.save(conversation);

        Optional<Message> last =
                messageRepository.findFirstByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(conversationId);

        return conversationMapper.toDTO(conversation, last.orElse(null));
    }

    // ADD MEMBER

    public ConversationDTO addGroupMember(Long conversationId, Long adminId, Long userId) {

        Boolean admin =
                memberRepository.isUserAdminOfConversation(conversationId, adminId);

        if (!Boolean.TRUE.equals(admin)) {
            throw new UnauthorizedException("Only admins can add members");
        }

        Optional<ConversationMember> existing =
                memberRepository.findByConversationAndUserId(conversationId, userId);

        if (existing.isEmpty()) {

            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new ConversationNotFoundException(conversationId));

            addMember(conversation, userId, false);
        }

        Optional<Message> last =
                messageRepository.findFirstByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(conversationId);

        return conversationMapper.toDTO(
                conversationRepository.findById(conversationId).get(),
                last.orElse(null)
        );
    }

    // REMOVE MEMBER

    public void removeGroupMember(Long conversationId, Long adminId, Long userId) {

        Boolean admin =
                memberRepository.isUserAdminOfConversation(conversationId, adminId);

        if (!Boolean.TRUE.equals(admin)) {
            throw new UnauthorizedException("Only admins can remove members");
        }

        ConversationMember member =
                memberRepository.findByConversationAndUserId(conversationId, userId)
                        .orElseThrow(() -> new UnauthorizedException("Member not found"));

        memberRepository.delete(member);
    }

    // INTERNAL

    private void addMember(Conversation conversation, Long userId, Boolean admin) {

        ConversationMember member = ConversationMember.builder()
                .conversation(conversation)
                .userId(userId)
                .username("user-" + userId)
                .isAdmin(admin)
                .build();

        memberRepository.save(member);
    }
}