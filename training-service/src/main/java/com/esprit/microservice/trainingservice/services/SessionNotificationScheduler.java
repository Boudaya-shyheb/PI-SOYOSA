package com.esprit.microservice.trainingservice.services;

import com.esprit.microservice.trainingservice.entities.Enrollment;
import com.esprit.microservice.trainingservice.entities.Session;
import com.esprit.microservice.trainingservice.repositories.EnrollmentRepository;
import com.esprit.microservice.trainingservice.repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class SessionNotificationScheduler {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private INotificationService notificationService;

    // Run every minute
    @Scheduled(cron = "0 * * * * *")
    public void checkUpcomingSessions() {
        // Find sessions starting in 15 minutes
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date targetTime = cal.getTime();
        
        // This is a naive implementation. For a real app, we'd use a more robust query.
        // We look for sessions on the target date.
        List<Session> allSessions = sessionRepository.findAll();
        
        for (Session session : allSessions) {
            if (session.getDate() == null || session.getStartTime() == null) continue;
            
            Date sessionStart = combineDateAndTime(session.getDate(), session.getStartTime());
            if (sessionStart == null) continue;
            
            // If session starts in exactly 15 minutes (or within the current minute window)
            long diffMillis = sessionStart.getTime() - System.currentTimeMillis();
            long diffMinutes = diffMillis / (60 * 1000);
            
            if (diffMinutes == 15) {
                // Notify all enrolled students
                List<Enrollment> enrollments = enrollmentRepository.findBySessionId(session.getId());
                for (Enrollment e : enrollments) {
                    notificationService.createNotification(e.getStudentId(), 
                        "Reminder: Your session for \"" + e.getTraining().getTitle() + "\" starts in 15 minutes!");
                }
            }
        }
    }

    private Date combineDateAndTime(Date date, java.sql.Time time) {
        if (date == null || time == null) return null;
        Calendar sCal = Calendar.getInstance();
        sCal.setTime(date);
        Calendar tCal = Calendar.getInstance();
        tCal.setTime(time);
        
        sCal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY));
        sCal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE));
        sCal.set(Calendar.SECOND, 0);
        sCal.set(Calendar.MILLISECOND, 0);
        return sCal.getTime();
    }
}
