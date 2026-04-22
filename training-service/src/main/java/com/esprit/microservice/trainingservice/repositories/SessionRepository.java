package com.esprit.microservice.trainingservice.repositories;

import com.esprit.microservice.trainingservice.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
    java.util.List<Session> findByTrainingId(int trainingId);
    org.springframework.data.domain.Page<Session> findByTrainingId(int trainingId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Session> findByTrainingIdAndStatusNot(int trainingId, com.esprit.microservice.trainingservice.entities.Status status, org.springframework.data.domain.Pageable pageable);
    
    org.springframework.data.domain.Page<Session> findByTrainingIdAndDate(int trainingId, java.util.Date date, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Session> findByTrainingIdAndStatusNotAndDate(int trainingId, com.esprit.microservice.trainingservice.entities.Status status, java.util.Date date, org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<Session> findByTrainingIdAndDateBetween(int trainingId, java.util.Date start, java.util.Date end, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Session> findByTrainingIdAndStatusNotAndDateBetween(int trainingId, com.esprit.microservice.trainingservice.entities.Status status, java.util.Date start, java.util.Date end, org.springframework.data.domain.Pageable pageable);
}
