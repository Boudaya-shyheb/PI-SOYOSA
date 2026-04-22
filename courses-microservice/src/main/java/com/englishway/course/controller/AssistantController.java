package com.englishway.course.controller;

import com.englishway.course.dto.AssistantChatRequest;
import com.englishway.course.dto.AssistantChatResponse;
import com.englishway.course.service.AssistantChatService;
import com.englishway.course.util.RequestContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final AssistantChatService assistantChatService;

    public AssistantController(AssistantChatService assistantChatService) {
        this.assistantChatService = assistantChatService;
    }

    @PostMapping("/chat")
    public AssistantChatResponse chat(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody AssistantChatRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return assistantChatService.chat(context, request);
    }
}