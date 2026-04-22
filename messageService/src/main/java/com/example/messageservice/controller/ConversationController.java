package com.example.messageservice.controller;

import com.example.messageservice.dto.ConversationDTO;
import com.example.messageservice.dto.CreateGroupRequest;
import com.example.messageservice.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getUserConversations(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = (Long) authentication.getDetails();

        List<ConversationDTO> conversations =
                conversationService.getUserConversationsByUserId(userId);

        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDTO> getConversation(
            @PathVariable Long id,
            Authentication authentication
    ) {

        ConversationDTO conversation =
                conversationService.getConversation(id);

        return ResponseEntity.ok(conversation);

    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationDTO> createConversation(
            @RequestBody Map<String, Object> request,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getDetails();

        log.info("Creating conversation by user {}", userId);

        Long profileId = ((Number) request.get("profileId")).longValue();

        ConversationDTO conversation;

        if (request.containsKey("participantProfileId")) {

            Long participantProfileId =
                    ((Number) request.get("participantProfileId")).longValue();

            conversation = conversationService
                    .createPrivateConversation(profileId, participantProfileId);

        } else {

            String name = (String) request.get("name");

            @SuppressWarnings("unchecked")
            List<Number> memberIds =
                    (List<Number>) request.get("memberProfileIds");

            List<Long> memberProfileIds =
                    memberIds.stream().map(Number::longValue).toList();

            conversation = conversationService
                    .createGroupConversation(profileId, name, memberProfileIds);
        }

        return ResponseEntity.ok(conversation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getDetails();

        conversationService.deleteConversation(id, userId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/rename")
    public ResponseEntity<ConversationDTO> renameGroup(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getDetails();

        String newName = request.get("name");

        ConversationDTO conversation =
                conversationService.renameGroup(id, userId, newName);

        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ConversationDTO> addMember(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getDetails();

        Long memberProfileId = request.get("userId");

        ConversationDTO conversation =
                conversationService.addGroupMember(id, userId, memberProfileId);

        return ResponseEntity.ok(conversation);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getDetails();

        conversationService.removeGroupMember(id, userId, memberId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conversations/group")
    public ResponseEntity<ConversationDTO> createGroup(
            @RequestBody CreateGroupRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long creatorUserId = (Long) authentication.getDetails();

        log.info("Creating group conversation '{}' by user {}", request.getName(), creatorUserId);

        ConversationDTO conversation = conversationService.createGroupConversation(
                creatorUserId,
                request.getName(),
                request.getMemberIds()
        );

        return ResponseEntity.ok(conversation);
    }



}