package com.esprit.microservice.trainingservice.services;

import com.esprit.microservice.trainingservice.entities.Enrollment;
import com.esprit.microservice.trainingservice.entities.Role;
import com.esprit.microservice.trainingservice.entities.Session;
import com.esprit.microservice.trainingservice.entities.Training;
import com.esprit.microservice.trainingservice.repositories.EnrollmentRepository;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private INotificationService notificationService;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    private SecurityUser student;
    private Training training;
    private Session session;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        student = SecurityUser.builder()
                .id(100L)
                .role(Role.STUDENT)
                .build();

        training = new Training();
        training.setId(1);
        training.setTitle("Java Unit Testing");

        session = new Session();
        session.setId(200);
        session.setTraining(training);
        session.setAvailableSpots(10);
        session.setDate(new java.util.Date());
        session.setStartTime(java.sql.Time.valueOf("09:00:00"));

        enrollment = new Enrollment();
        enrollment.setId(300);
        enrollment.setStudentId(100L);
        enrollment.setTraining(training);
    }

    @Test
    void buyTraining_ShouldSaveEnrollment() {
        when(trainingRepository.findById(1)).thenReturn(Optional.of(training));
        when(enrollmentRepository.findByStudentIdAndTrainingId(100L, 1)).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArgument(0));

        Enrollment result = enrollmentService.buyTraining(1, student);

        assertNotNull(result);
        assertEquals(training, result.getTraining());
        assertEquals(100L, result.getStudentId());
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void buyTraining_WhenAlreadyEnrolled_ShouldThrowException() {
        when(trainingRepository.findById(1)).thenReturn(Optional.of(training));
        when(enrollmentRepository.findByStudentIdAndTrainingId(100L, 1)).thenReturn(Optional.of(enrollment));

        assertThrows(RuntimeException.class, () -> enrollmentService.buyTraining(1, student));
    }

    @Test
    void enrollInSession_WhenScheduleConflict_ShouldThrowException() {
        Enrollment existingEnrollment = new Enrollment();
        Session competingSession = new Session();
        competingSession.setId(222);
        competingSession.setDate(session.getDate());
        competingSession.setStartTime(session.getStartTime());
        competingSession.setTraining(training);
        
        existingEnrollment.setSession(competingSession);

        when(trainingRepository.findById(1)).thenReturn(Optional.of(training));
        when(sessionRepository.findById(200)).thenReturn(Optional.of(session));
        when(enrollmentRepository.findByStudentIdAndTrainingId(100L, 1)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.findByStudentId(100L)).thenReturn(Collections.singletonList(existingEnrollment));

        Exception exception = assertThrows(RuntimeException.class, () -> 
            enrollmentService.enrollInSession(1, 200, student)
        );
        assertTrue(exception.getMessage().contains("Schedule Conflict"));
    }

    @Test
    void issueCertificate_WhenPresent_ShouldIssue() {
        enrollment.setPresent(true);
        when(enrollmentRepository.findById(300)).thenReturn(Optional.of(enrollment));

        enrollmentService.issueCertificate(300);

        verify(enrollmentRepository, times(1)).issueCertificateStatus(300);
        verify(notificationService, times(1)).createNotification(eq(100L), anyString());
    }

    @Test
    void issueCertificate_WhenNotPresent_ShouldThrowException() {
        enrollment.setPresent(false);
        when(enrollmentRepository.findById(300)).thenReturn(Optional.of(enrollment));

        assertThrows(RuntimeException.class, () -> enrollmentService.issueCertificate(300));
    }
}
