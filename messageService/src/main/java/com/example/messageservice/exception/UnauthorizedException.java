package com.example.messageservice.exception;

public class UnauthorizedException extends ChatException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
