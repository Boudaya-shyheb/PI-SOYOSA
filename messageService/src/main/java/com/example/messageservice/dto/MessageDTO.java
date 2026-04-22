package com.example.messageservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("content")
    private String content;

    @JsonProperty("dateSent")
    private LocalDateTime dateSent;

    @JsonProperty("messageType")
    private String messageType;

    @JsonProperty("senderId")
    private Long senderId;

    @JsonProperty("senderUsername")
    private String senderUsername;

    @JsonProperty("messageRoomId")
    private Long messageRoomId;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("isDeleted")
    private Boolean isDeleted;


}
