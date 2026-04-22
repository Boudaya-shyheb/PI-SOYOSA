package com.example.gameservice.contorller;

import com.example.gameservice.entities.Score;
import com.example.gameservice.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScoreController {
    private final ScoreService scoreService;

    @PostMapping("/game/{gameId}")
    public Score addScore(@PathVariable Long gameId,
                          @RequestBody Score score) {
        return scoreService.addScore(gameId, score);
    }

    @GetMapping("/{id}")
    public Score getById(@PathVariable Long id) {
        return scoreService.getScoreById(id);
    }

    @GetMapping
    public List<Score> getAll() {
        return scoreService.getAllScores();
    }

    @GetMapping("/game/{gameId}")
    public List<Score> getByGame(@PathVariable Long gameId) {
        return scoreService.getScoresByGame(gameId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        scoreService.deleteScore(id);
    }
}
