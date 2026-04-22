package com.esprit.microservice.trainingservice.services;

import com.esprit.microservice.trainingservice.entities.Enrollment;
import com.esprit.microservice.trainingservice.security.SecurityUser;

import java.util.List;

public interface IEnrollmentService {
    Enrollment buyTraining(int trainingId, SecurityUser student);
    Enrollment enrollInSession(int trainingId, int sessionId, SecurityUser student);
    org.springframework.data.domain.Page<Enrollment> getStudentEnrollments(SecurityUser student, org.springframework.data.domain.Pageable pageable);
    boolean isStudentEnrolledInTraining(int trainingId, SecurityUser student);
    List<Enrollment> getSessionEnrollments(int sessionId);
    Enrollment togglePresence(int enrollmentId, boolean present);
    Enrollment issueCertificate(int enrollmentId);
    List<Enrollment> getUpcomingEnrollments(SecurityUser student);
    boolean isEligibleToReview(int trainingId, SecurityUser student);
    List<Enrollment> getStudentCertificates(SecurityUser student);

    //void cancelEnrollment(int , SecurityUser );
}
