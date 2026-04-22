package com.example.gameservice.service;

import com.example.gameservice.entities.Game;
import com.example.gameservice.entities.Score;
import com.example.gameservice.reposotories.GameRepository;
import com.example.gameservice.reposotories.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreService {
    private final ScoreRepository scoreRepository;

    private final GameRepository gameRepository;

    public Score addScore(Long gameId, Score score) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        score.setGame(game);

        return scoreRepository.save(score);
    }

    public Score getScoreById(Long id) {
        return scoreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Score not found"));
    }

    public List<Score> getAllScores() {
        return scoreRepository.findAll();
    }

    public List<Score> getScoresByGame(Long gameId) {
        return scoreRepository.findByGameId(gameId);
    }

    public void deleteScore(Long id) {
        scoreRepository.deleteById(id);
    }
}
