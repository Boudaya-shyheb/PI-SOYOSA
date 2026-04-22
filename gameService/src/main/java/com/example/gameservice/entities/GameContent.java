package com.example.gameservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "game_contents")
public class GameContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // QUIZ fields
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;

    // WORD SCRAMBLE
    private String word;

    // CROSSWORD
    private String clue;
    private String answer;

    private int points;

    @ManyToOne
    @JoinColumn(name = "game_id")
    @JsonIgnore
    private Game game;
}
