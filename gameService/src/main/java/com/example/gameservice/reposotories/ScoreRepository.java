package com.example.gameservice.reposotories;

import com.example.gameservice.entities.Score;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreRepository extends JpaRepository<Score,Long> {
    List<Score> findByGameId(Long gameId);

    List<Score> findByStudentId(Long studentId);
}
