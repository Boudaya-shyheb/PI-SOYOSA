package com.example.gameservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScoreDto {
    private Long idScore;
    private int score;
    private LocalDateTime playedAt;
    private Long studentId;
}
