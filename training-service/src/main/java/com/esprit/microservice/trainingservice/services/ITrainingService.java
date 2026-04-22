package com.esprit.microservice.trainingservice.services;

import com.esprit.microservice.trainingservice.dto.TrainingCreateDTO;
import com.esprit.microservice.trainingservice.dto.TrainingUpdateDTO;
import com.esprit.microservice.trainingservice.dto.SessionDTO;
import com.esprit.microservice.trainingservice.entities.Session;
import com.esprit.microservice.trainingservice.entities.Training;
import com.esprit.microservice.trainingservice.security.SecurityUser;

import java.util.List;

public interface ITrainingService {

     Training addTraining(TrainingCreateDTO training, SecurityUser tutor);
     Training updateTraining(int id, TrainingUpdateDTO dto, SecurityUser user);
     org.springframework.data.domain.Page<Training> getTrainings(SecurityUser user, String search, org.springframework.data.domain.Pageable pageable);
     Training getTraining(int id);
     void deleteTraining(int id, SecurityUser user);

     // Session methods
     Session addSession(int trainingId, SessionDTO sessionDTO, SecurityUser user);
     org.springframework.data.domain.Page<Session> getSessionsByTraining(int trainingId, SecurityUser user, java.util.Date startDate, java.util.Date endDate, org.springframework.data.domain.Pageable pageable);
     Session updateSession(int trainingId, int sessionId, SessionDTO sessionDTO, SecurityUser user);
     void deleteSession(int trainingId, int sessionId, SecurityUser user);
     Session markSessionAsCompleted(int trainingId, int sessionId, SecurityUser user);

     // Review methods
     com.esprit.microservice.trainingservice.entities.Review addReview(int trainingId, com.esprit.microservice.trainingservice.dto.ReviewDTO reviewDTO, SecurityUser user);
     List<com.esprit.microservice.trainingservice.entities.Review> getReviewsByTraining(int trainingId);

     List<Training> getRecommendations(SecurityUser user);
}
