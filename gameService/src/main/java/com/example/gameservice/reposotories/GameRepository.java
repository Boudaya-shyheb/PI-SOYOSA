package com.example.gameservice.reposotories;

import com.example.gameservice.entities.Game;
import com.example.gameservice.entities.GameType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game,Long> {
    List<Game> findByTypeAndLevel(GameType type, String level);
}
