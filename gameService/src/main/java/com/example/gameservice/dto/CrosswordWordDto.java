package com.example.gameservice.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrosswordWordDto extends GameContentDTO {
    private String word;
    private String clue;
    private int positionX;
    private int positionY;
    private boolean horizontal;
}

