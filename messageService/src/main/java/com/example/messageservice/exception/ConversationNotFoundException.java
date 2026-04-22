package com.example.messageservice.exception;

public class ConversationNotFoundException extends ChatException {

    public ConversationNotFoundException(Long conversationId) {
        super("Conversation not found: " + conversationId);
    }

    public ConversationNotFoundException(String message) {
        super(message);
    }
}
