package com.englishway.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AssistantChatRequest {
    private String model;
    private Double temperature;

    @Valid
    @NotEmpty(message = "At least one message is required")
    private List<AssistantChatMessage> messages;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public List<AssistantChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<AssistantChatMessage> messages) {
        this.messages = messages;
    }
}