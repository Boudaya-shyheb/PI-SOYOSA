package com.example.messageservice.exception;

public class MessageNotFoundException extends ChatException {

    public MessageNotFoundException(Long messageId) {
        super("Message not found: " + messageId);
    }

    public MessageNotFoundException(String message) {
        super(message);
    }
}
