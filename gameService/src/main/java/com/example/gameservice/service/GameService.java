package com.example.gameservice.service;

import com.example.gameservice.entities.Game;
import com.example.gameservice.reposotories.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;


    public Game createGame(Game game){
        return gameRepository.save(game);
    }

    public Game getGameById(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public List<Game> getGamesByTypeAndLevel(com.example.gameservice.entities.GameType type, String level) {
        return gameRepository.findByTypeAndLevel(type, level);
    }

    public Game updateGame(Long id, Game updatedGame) {

        Game existingGame = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        existingGame.setTitle(updatedGame.getTitle());
        existingGame.setType(updatedGame.getType());
        existingGame.setLevel(updatedGame.getLevel());

        return gameRepository.save(existingGame);
    }

    public void deleteGame(Long id) {
        gameRepository.deleteById(id);
    }


}
