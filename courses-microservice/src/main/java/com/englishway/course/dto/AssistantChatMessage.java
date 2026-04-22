package com.englishway.course.dto;

import jakarta.validation.constraints.NotBlank;

public class AssistantChatMessage {
    @NotBlank
    private String role;

    @NotBlank
    private String content;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}