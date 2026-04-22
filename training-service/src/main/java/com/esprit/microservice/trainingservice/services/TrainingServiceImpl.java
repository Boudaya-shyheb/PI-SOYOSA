package com.esprit.microservice.trainingservice.services;

import com.esprit.microservice.trainingservice.dto.SessionDTO;
import com.esprit.microservice.trainingservice.dto.TrainingCreateDTO;
import com.esprit.microservice.trainingservice.dto.TrainingUpdateDTO;
import com.esprit.microservice.trainingservice.entities.Level;
import com.esprit.microservice.trainingservice.entities.Session;
import com.esprit.microservice.trainingservice.entities.Status;
import com.esprit.microservice.trainingservice.entities.Training;
import com.esprit.microservice.trainingservice.repositories.SessionRepository;
import com.esprit.microservice.trainingservice.repositories.TrainingRepository;
import com.esprit.microservice.trainingservice.repositories.EnrollmentRepository;
import com.esprit.microservice.trainingservice.repositories.ReviewRepository;
import com.esprit.microservice.trainingservice.security.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.List;

@Service
public class TrainingServiceImpl implements ITrainingService {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private com.esprit.microservice.trainingservice.repositories.UserPlacementResultRepository placementResultRepository;

    @Autowired
    private ReviewModerationService moderationService;

    @Override
    public List<Training> getRecommendations(SecurityUser user) {
        // 1. Determine user's current level
        Level currentLevel = Level.A1;
        
        // Check placement test first
        var placement = placementResultRepository.findByStudentId(user.getId());
        if (placement.isPresent()) {
            currentLevel = placement.get().getDeterminedLevel();
        }

        // Check enrollments to see if they've progressed further
        List<com.esprit.microservice.trainingservice.entities.Enrollment> enrollments = enrollmentRepository.findByStudentId(user.getId());
        for (var e : enrollments) {
            if (e.getTraining().getLevel().ordinal() >= currentLevel.ordinal()) {
                currentLevel = e.getTraining().getLevel();
            }
        }

        // 2. Fetch recommendations
        List<Integer> enrolledIds = enrollments.stream()
                .map(e -> e.getTraining().getId())
                .collect(java.util.stream.Collectors.toList());

        // Priority 1: SAME Level
        List<Training> filtered = trainingRepository.findByLevel(currentLevel).stream()
                .filter(t -> !enrolledIds.contains(t.getId()))
                .limit(3)
                .collect(java.util.stream.Collectors.toList());

        // Priority 2: NEXT Level (Progression)
        if (filtered.size() < 4 && currentLevel.ordinal() < Level.values().length - 1) {
            Level nextLevel = Level.values()[currentLevel.ordinal() + 1];
            List<Training> next = trainingRepository.findByLevel(nextLevel).stream()
                    .filter(t -> !enrolledIds.contains(t.getId()))
                    .limit(2)
                    .collect(java.util.stream.Collectors.toList());
            filtered.addAll(next);
        }

        // Priority 3: Top Rated Fallback (Only if Rating is high > 4.0)
        if (filtered.size() < 4) {
            List<Training> all = trainingRepository.findAll();
            all.forEach(this::calculateAverageRating);
            List<Training> topRated = all.stream()
                    .filter(t -> !enrolledIds.contains(t.getId()))
                    .filter(t -> !filtered.contains(t))
                    .filter(t -> t.getAverageRating() >= 4.0) // Only highly rated
                    .sorted((t1, t2) -> Double.compare(t2.getAverageRating(), t1.getAverageRating()))
                    .limit(5 - filtered.size())
                    .collect(java.util.stream.Collectors.toList());
            filtered.addAll(topRated);
        }

        return filtered;
    }
    public Training addTraining (TrainingCreateDTO dto, SecurityUser tutor){
        Training training = new Training();
        training.setTitle(dto.getTitle());
        training.setDescription(dto.getDescription());
        training.setLevel(dto.getLevel());
        training.setPrice(dto.getPrice());
        training.setImageUrl(dto.getImageUrl());
        training.setType(dto.getType());
        training.setMeetingLink(dto.getMeetingLink());
        training.setLocation(dto.getLocation());
        training.setRoom(dto.getRoom());
        training.setLatitude(dto.getLatitude());
        training.setLongitude(dto.getLongitude());
        training.setCreatedByUserId(tutor.getId());
        return trainingRepository.save(training);
    }

    @Override
    public Training updateTraining(int id, TrainingUpdateDTO dto, SecurityUser user) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        if (training.getCreatedByUserId() != null && !training.getCreatedByUserId().equals(user.getId()) && user.getRole() != com.esprit.microservice.trainingservice.entities.Role.ADMIN) {
            throw new RuntimeException("Unauthorized: You do not own this training");
        }

        training.setTitle(dto.getTitle());
        training.setDescription(dto.getDescription());
        training.setLevel(dto.getLevel());
        training.setPrice(dto.getPrice());
        training.setImageUrl(dto.getImageUrl());
        training.setType(dto.getType());
        training.setMeetingLink(dto.getMeetingLink());
        training.setLocation(dto.getLocation());
        training.setRoom(dto.getRoom());
        training.setLatitude(dto.getLatitude());
        training.setLongitude(dto.getLongitude());
        return trainingRepository.save(training);
    }

    @Override
    public org.springframework.data.domain.Page<Training> getTrainings(SecurityUser user, String search, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Training> page;
        if (search != null && !search.trim().isEmpty()) {
            page = trainingRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        } else {
            page = trainingRepository.findAll(pageable);
        }

        if (user != null && user.getRole() == com.esprit.microservice.trainingservice.entities.Role.TUTOR) {
            // NOTE: For Tutor, we ideally want a page filtered by tutorId.
            // But since the current logic filters the whole list, let's stick to that for now or improve it.
            // Actually, Spring Data JPA Page handles filtering better at repository level.
            // For now, let's keep the stream logic but it's not ideal for pagination if we filter AFTER fetching a page.
            // Ideally we'd have a findByCreatedByUserId(...) method.
            page.getContent().forEach(this::calculateAverageRating);
            return page;
        }
        page.getContent().forEach(this::calculateAverageRating);
        return page;
    }

    @Override
    public Training getTraining(int id) {
        Training training = trainingRepository.findById(id).orElseThrow(() -> new RuntimeException("Training not found"));
        calculateAverageRating(training);
        return training;
    }

    @Override
    public void deleteTraining(int id, SecurityUser user) {
        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        if (training.getCreatedByUserId() != null && !training.getCreatedByUserId().equals(user.getId()) && user.getRole() != com.esprit.microservice.trainingservice.entities.Role.ADMIN) {
            throw new RuntimeException("Unauthorized: You do not own this training");
        }
        trainingRepository.deleteById(id);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Session addSession(int trainingId, SessionDTO dto, SecurityUser user) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        if (training.getCreatedByUserId() != null && !training.getCreatedByUserId().equals(user.getId()) && user.getRole() != com.esprit.microservice.trainingservice.entities.Role.ADMIN) {
            throw new RuntimeException("Unauthorized: Only the owner can add sessions");
        }

        Session session = new Session();
        mapDtoToSession(dto, session);
        session.setTraining(training);
        session.setStatus(Status.PLANNED);
        session.setAvailableSpots(dto.getMaxParticipants());
        return sessionRepository.save(session);
    }

    @Override
    public org.springframework.data.domain.Page<Session> getSessionsByTraining(int trainingId, SecurityUser user, java.util.Date startDate, java.util.Date endDate, org.springframework.data.domain.Pageable pageable) {
        boolean isStudent = user.getRole() == com.esprit.microservice.trainingservice.entities.Role.STUDENT;
        
        if (startDate != null && endDate != null) {
            if (isStudent) {
                return sessionRepository.findByTrainingIdAndStatusNotAndDateBetween(trainingId, Status.COMPLETED, startDate, endDate, pageable);
            }
            return sessionRepository.findByTrainingIdAndDateBetween(trainingId, startDate, endDate, pageable);
        } else if (startDate != null) {
            if (isStudent) {
                return sessionRepository.findByTrainingIdAndStatusNotAndDate(trainingId, Status.COMPLETED, startDate, pageable);
            }
            return sessionRepository.findByTrainingIdAndDate(trainingId, startDate, pageable);
        }
        
        if (isStudent) {
            return sessionRepository.findByTrainingIdAndStatusNot(trainingId, Status.COMPLETED, pageable);
        }
        return sessionRepository.findByTrainingId(trainingId, pageable);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Session updateSession(int trainingId, int sessionId, SessionDTO dto, SecurityUser user) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        if (training.getCreatedByUserId() != null && !training.getCreatedByUserId().equals(user.getId()) && user.getRole() != com.esprit.microservice.trainingservice.entities.Role.ADMIN) {
            throw new RuntimeException("Unauthorized: Only the owner can update sessions");
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Status oldStatus = session.getStatus();
        mapDtoToSession(dto, session);
        if (dto.getStatus() == null) {
            session.setStatus(oldStatus);
        }
        return sessionRepository.save(session);
    }

    @Override
    public void deleteSession(int trainingId, int sessionId, SecurityUser user) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        if (training.getCreatedByUserId() != null && !training.getCreatedByUserId().equals(user.getId()) && user.getRole() != com.esprit.microservice.trainingservice.entities.Role.ADMIN) {
            throw new RuntimeException("Unauthorized: Only the owner can delete sessions");
        }

        sessionRepository.deleteById(sessionId);
    }

    @Override
    public Session markSessionAsCompleted(int trainingId, int sessionId, SecurityUser user) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        if (training.getCreatedByUserId() != null && !training.getCreatedByUserId().equals(user.getId()) && user.getRole() != com.esprit.microservice.trainingservice.entities.Role.ADMIN) {
            throw new RuntimeException("Unauthorized: Only the owner can complete sessions");
        }

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setStatus(Status.COMPLETED);
        return sessionRepository.save(session);
    }

    private void mapDtoToSession(SessionDTO dto, Session session) {
        session.setDate(dto.getDate());
        try {
            if (dto.getStartTime() != null) {
                String timeStr = dto.getStartTime();
                if (timeStr.split(":").length == 2) timeStr += ":00";
                session.setStartTime(Time.valueOf(timeStr));
            }
        } catch (Exception e) {
            System.err.println("Error parsing time: " + dto.getStartTime());
        }
        if (dto.getStatus() != null) {
            session.setStatus(dto.getStatus());
        }
        session.setDuration(dto.getDuration());
        session.setMaxParticipants(dto.getMaxParticipants());
    }

    @Autowired
    private IEnrollmentService enrollmentService;

    @Override
    public com.esprit.microservice.trainingservice.entities.Review addReview(int trainingId, com.esprit.microservice.trainingservice.dto.ReviewDTO reviewDTO, SecurityUser user) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        if (!enrollmentService.isEligibleToReview(trainingId, user)) {
            throw new RuntimeException("You must attend at least one completed session to leave a review");
        }

        if (reviewRepository.existsByTrainingIdAndStudentId(trainingId, user.getId())) {
            throw new RuntimeException("You have already reviewed this training");
        }

        if (moderationService.containsBadWords(reviewDTO.getComment())) {
            throw new IllegalArgumentException("Your review contains inappropriate language. Please keep it professional!");
        }

        com.esprit.microservice.trainingservice.entities.Review review = new com.esprit.microservice.trainingservice.entities.Review();
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setStudentId(user.getId());
        review.setTraining(training);

        return reviewRepository.save(review);
    }

    @Override
    public List<com.esprit.microservice.trainingservice.entities.Review> getReviewsByTraining(int trainingId) {
        return reviewRepository.findByTrainingId(trainingId);
    }

    private void calculateAverageRating(Training training) {
        if (training.getReviews() == null || training.getReviews().isEmpty()) {
            training.setAverageRating(0.0);
            return;
        }
        double sum = training.getReviews().stream().mapToDouble(com.esprit.microservice.trainingservice.entities.Review::getRating).sum();
        training.setAverageRating(sum / training.getReviews().size());
    }
}
