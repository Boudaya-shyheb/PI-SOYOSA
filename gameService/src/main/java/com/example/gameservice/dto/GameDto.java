package com.example.gameservice.dto;


import com.example.gameservice.entities.GameType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GameDto {
    private Long idGame;
    private String title;
    private GameType type;
    private String level;
    private LocalDateTime createdAt;
    private List<GameContentDTO> contents;
}
