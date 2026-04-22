package com.example.gameservice.contorller;

import com.example.gameservice.entities.Game;
import com.example.gameservice.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GameController {
    private final GameService gameService;

    @PostMapping
    public Game createGame(@RequestBody Game game) {
        return gameService.createGame(game);
    }

    @GetMapping("/{id}")
    public Game getGameById(@PathVariable Long id) {
        return gameService.getGameById(id);
    }

    @GetMapping
    public List<Game> getAllGames() {
        return gameService.getAllGames();
    }

    @GetMapping("/type/{type}")
    public List<Game> getGamesByTypeAndLevel(
            @PathVariable com.example.gameservice.entities.GameType type, 
            @RequestParam(required = false) String level) {
        if (level != null) {
            return gameService.getGamesByTypeAndLevel(type, level);
        }
        // Fallback for just type
        return gameService.getAllGames().stream()
                .filter(g -> g.getType() == type)
                .toList();
    }

    @PutMapping("/{id}")
    public Game updateGame(@PathVariable Long id, @RequestBody Game game) {
        return gameService.updateGame(id, game);
    }

    @DeleteMapping("/{id}")
    public void deleteGame(@PathVariable Long id) {
        gameService.deleteGame(id);
    }
}
