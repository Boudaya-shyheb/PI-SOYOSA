package com.esprit.microservice.trainingservice.services;

import com.esprit.microservice.trainingservice.dto.SessionDTO;
import com.esprit.microservice.trainingservice.dto.TrainingCreateDTO;
import com.esprit.microservice.trainingservice.dto.TrainingUpdateDTO;
import com.esprit.microservice.trainingservice.entities.Role;
import com.esprit.microservice.trainingservice.entities.Session;
import com.esprit.microservice.trainingservice.entities.Status;
import com.esprit.microservice.trainingservice.entities.Training;
import com.esprit.microservice.trainingservice.repositories.EnrollmentRepository;
import com.esprit.microservice.trainingservice.repositories.ReviewRepository;
import com.esprit.microservice.trainingservice.repositories.SessionRepository;
import com.esprit.microservice.trainingservice.repositories.TrainingRepository;
import com.esprit.microservice.trainingservice.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceImplTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private IEnrollmentService enrollmentService;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private SecurityUser tutor;
    private Training training;

    @BeforeEach
    void setUp() {
        tutor = SecurityUser.builder()
                .id(1L)
                .role(Role.TUTOR)
                .build();

        training = new Training();
        training.setId(1);
        training.setTitle("Java Basics");
        training.setCreatedByUserId(1L);
    }

    @Test
    void addTraining_ShouldSaveTraining() {
        TrainingCreateDTO dto = new TrainingCreateDTO();
        dto.setTitle("Java Basics");
        dto.setPrice(49.99);
        dto.setLatitude(10.0);
        dto.setLongitude(20.0);

        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Training result = trainingService.addTraining(dto, tutor);

        assertNotNull(result);
        assertEquals("Java Basics", result.getTitle());
        assertEquals(10.0, result.getLatitude());
        assertEquals(20.0, result.getLongitude());
        verify(trainingRepository, times(1)).save(any(Training.class));
    }

    @Test
    void updateTraining_WhenOwner_ShouldUpdate() {
        TrainingUpdateDTO dto = new TrainingUpdateDTO();
        dto.setTitle("Java Advanced");
        dto.setPrice(59.99);
        dto.setLatitude(11.0);

        when(trainingRepository.findById(1)).thenReturn(Optional.of(training));
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Training result = trainingService.updateTraining(1, dto, tutor);

        assertEquals("Java Advanced", result.getTitle());
        assertEquals(11.0, result.getLatitude());
        verify(trainingRepository, times(1)).save(any(Training.class));
    }

    @Test
    void updateTraining_WhenNotOwner_ShouldThrowException() {
        SecurityUser stranger = SecurityUser.builder()
                .id(2L)
                .role(Role.TUTOR)
                .build();

        when(trainingRepository.findById(1)).thenReturn(Optional.of(training));

        assertThrows(RuntimeException.class, () -> trainingService.updateTraining(1, new TrainingUpdateDTO(), stranger));
    }

    @Test
    void addSession_ShouldCreateSession() {
        SessionDTO dto = new SessionDTO();
        dto.setMaxParticipants(20);
        dto.setStartTime("10:00");

        when(trainingRepository.findById(1)).thenReturn(Optional.of(training));
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Session result = trainingService.addSession(1, dto, tutor);

        assertNotNull(result);
        assertEquals(Status.PLANNED, result.getStatus());
        assertEquals(20, result.getAvailableSpots());
        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    @Test
    void calculateAverageRating_ShouldHandleEmptyReviews() {
        training.setReviews(java.util.Collections.emptyList());
        
        when(trainingRepository.findById(1)).thenReturn(Optional.of(training));
        
        Training result = trainingService.getTraining(1);
        assertEquals(0.0, result.getAverageRating());
    }
}
