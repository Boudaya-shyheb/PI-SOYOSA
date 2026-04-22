package com.example.messageservice.exception;

public class UserNotFoundException extends ChatException {

    public UserNotFoundException(Long profileId) {
        super("User profile not found: " + profileId);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
