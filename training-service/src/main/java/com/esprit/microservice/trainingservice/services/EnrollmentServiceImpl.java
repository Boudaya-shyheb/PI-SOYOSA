package com.esprit.microservice.trainingservice.services;

import com.esprit.microservice.trainingservice.entities.Enrollment;
import com.esprit.microservice.trainingservice.entities.Role;
import com.esprit.microservice.trainingservice.entities.Session;
import com.esprit.microservice.trainingservice.entities.Training;
import com.esprit.microservice.trainingservice.repositories.EnrollmentRepository;
import com.esprit.microservice.trainingservice.repositories.SessionRepository;
import com.esprit.microservice.trainingservice.repositories.TrainingRepository;
import com.esprit.microservice.trainingservice.security.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentServiceImpl implements IEnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private INotificationService notificationService;

    @Override
    @Transactional
    public Enrollment buyTraining(int trainingId, SecurityUser student) {
        if (student.getRole() != Role.STUDENT) {
            throw new RuntimeException("Only students can buy trainings");
        }

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found with id: " + trainingId));

        Optional<Enrollment> existingEnrollment = enrollmentRepository.findByStudentIdAndTrainingId(student.getId(), trainingId);
        if (existingEnrollment.isPresent()) {
            throw new RuntimeException("Student is already enrolled in this training");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getId());
        enrollment.setTraining(training);

        return enrollmentRepository.save(enrollment);
    }

    @Override
    @Transactional
    public Enrollment enrollInSession(int trainingId, int sessionId, SecurityUser student) {
        if (student.getRole() != Role.STUDENT) {
            throw new RuntimeException("Only students can enroll in sessions");
        }

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found with id: " + trainingId));

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));

        if (session.getTraining().getId() != trainingId) {
             throw new RuntimeException("This session does not belong to the specified training");
        }

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndTrainingId(student.getId(), trainingId)
                .orElseThrow(() -> new RuntimeException("Student must buy the training before enrolling in a session"));

        if (enrollment.getSession() != null) {
            throw new RuntimeException("Student is already enrolled in a session for this training");
        }

        if (session.getAvailableSpots() <= 0) {
            throw new RuntimeException("No available spots in this session");
        }

        List<Enrollment> studentEnrollments = enrollmentRepository.findByStudentId(student.getId());
        
        java.util.Date newStart = combineDateAndTime(session.getDate(), session.getStartTime());

        for (Enrollment existing : studentEnrollments) {
            if (existing.getSession() != null && existing.getSession().getId() != sessionId) {
                Session s = existing.getSession();
                java.util.Date existStart = combineDateAndTime(s.getDate(), s.getStartTime());

                if (newStart != null && newStart.equals(existStart)) {
                    throw new RuntimeException("Schedule Conflict: You are already enrolled in another session starting at this exact time in \"" 
                        + s.getTraining().getTitle() + "\"");
                }
            }
        }

        session.setAvailableSpots(session.getAvailableSpots() - 1);
        sessionRepository.save(session);

        enrollment.setSession(session);
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public org.springframework.data.domain.Page<Enrollment> getStudentEnrollments(SecurityUser student, org.springframework.data.domain.Pageable pageable) {
        return enrollmentRepository.findByStudentId(student.getId(), pageable);
    }
    
    @Override
    public boolean isStudentEnrolledInTraining(int trainingId, SecurityUser student) {
        if (student == null || student.getId() == null) return false;
        return enrollmentRepository.findByStudentIdAndTrainingId(student.getId(), trainingId).isPresent();
    }

    @Override
    public List<Enrollment> getSessionEnrollments(int sessionId) {
        return enrollmentRepository.findBySessionId(sessionId);
    }

    @Override
    @Transactional
    public Enrollment togglePresence(int enrollmentId, boolean present) {
        enrollmentRepository.updatePresenceStatus(enrollmentId, present);
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));
    }

    @Override
    @Transactional
    public Enrollment issueCertificate(int enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with id: " + enrollmentId));
        
        if (!enrollment.isPresent()) {
            throw new RuntimeException("Cannot issue certificate: Student was not present in the session");
        }
        
        // Capture data needed for notification BEFORE clearing context
        String trainingTitle = enrollment.getTraining().getTitle();
        Long studentId = enrollment.getStudentId();
        
        enrollmentRepository.issueCertificateStatus(enrollmentId);
        
        // Trigger notification using captured data
        notificationService.createNotification(studentId, 
            "Congratulations! You have been issued a certificate for \"" + trainingTitle + "\".", 
            "CERTIFICATE", 
            (long) enrollment.getTraining().getId());
            
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment refetch failed"));
    }

    @Override
    public List<Enrollment> getUpcomingEnrollments(SecurityUser student) {
        if (student == null) return java.util.Collections.emptyList();
        
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        java.util.Date now = new java.util.Date();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(now);
        cal.add(java.util.Calendar.HOUR, 24);
        java.util.Date tomorrow = cal.getTime();

        return enrollments.stream()
                .filter(e -> e.getSession() != null)
                .filter(e -> {
                    java.util.Date sessionFullDate = combineDateAndTime(e.getSession().getDate(), e.getSession().getStartTime());
                    if (sessionFullDate == null) return false;
                    return (sessionFullDate.after(now) && sessionFullDate.before(tomorrow)) || e.isCertificateIssued();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean isEligibleToReview(int trainingId, SecurityUser student) {
        if (student == null || student.getId() == null) return false;
        return enrollmentRepository.isEligibleToReview(student.getId(), trainingId);
    }

    @Override
    public List<Enrollment> getStudentCertificates(SecurityUser student) {
        if (student == null) return java.util.Collections.emptyList();
        return enrollmentRepository.findByStudentIdAndCertificateIssuedTrue(student.getId());
    }



    private java.util.Date combineDateAndTime(java.util.Date date, java.sql.Time time) {
        if (date == null || time == null) return null;
        java.util.Calendar sCal = java.util.Calendar.getInstance();
        sCal.setTime(date);
        java.util.Calendar tCal = java.util.Calendar.getInstance();
        tCal.setTime(time);
        
        sCal.set(java.util.Calendar.HOUR_OF_DAY, tCal.get(java.util.Calendar.HOUR_OF_DAY));
        sCal.set(java.util.Calendar.MINUTE, tCal.get(java.util.Calendar.MINUTE));
        sCal.set(java.util.Calendar.SECOND, tCal.get(java.util.Calendar.SECOND));
        return sCal.getTime();
    }
}
