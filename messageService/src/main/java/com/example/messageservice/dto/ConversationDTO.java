package com.example.messageservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("isGroup")
    private Boolean isGroup;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("createdById")
    private Long createdById;

    @JsonProperty("createdByUsername")
    private String createdByUsername;

    @JsonProperty("members")
    private List<ConversationMemberDTO> members;

    @JsonProperty("lastMessage")
    private MessageDTO lastMessage;

}