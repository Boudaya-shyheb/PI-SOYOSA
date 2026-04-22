package com.example.gameservice.service;

import com.example.gameservice.entities.Game;
import com.example.gameservice.reposotories.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    private Game game;
    private Long gameId = 1L;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.setId(gameId);
        game.setTitle("Jungle Quiz");
    }

    @Test
    void getGameById_Success() {
        // Arrange
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // Act
        Game result = gameService.getGameById(gameId);

        // Assert
        assertNotNull(result);
        assertEquals("Jungle Quiz", result.getTitle());
        verify(gameRepository).findById(gameId);
    }

    @Test
    void getGameById_NotFound() {
        // Arrange
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            gameService.getGameById(gameId)
        );
        assertEquals("Game not found", exception.getMessage());
    }

    @Test
    void createGame_Success() {
        // Arrange
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        // Act
        Game result = gameService.createGame(new Game());

        // Assert
        assertNotNull(result);
        assertEquals(gameId, result.getId());
        verify(gameRepository).save(any(Game.class));
    }

    @Test
    void getAllGames_Success() {
        // Arrange
        when(gameRepository.findAll()).thenReturn(List.of(game));

        // Act
        List<Game> result = gameService.getAllGames();

        // Assert
        assertEquals(1, result.size());
        verify(gameRepository).findAll();
    }
}
