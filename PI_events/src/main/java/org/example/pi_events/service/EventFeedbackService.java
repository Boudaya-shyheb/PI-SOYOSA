package org.example.pi_events.service;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.EventFeedbackDTO;
import org.example.pi_events.entity.Event;
import org.example.pi_events.entity.EventFeedback;
import org.example.pi_events.repository.EventFeedbackRepository;
import org.example.pi_events.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventFeedbackService {

    private final EventFeedbackRepository feedbackRepository;
    private final EventRepository eventRepository;

    public EventFeedbackDTO addFeedback(Long eventId, Integer rating, String comment) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        EventFeedback feedback = new EventFeedback();
        feedback.setRating(rating);
        feedback.setComment(comment);
        feedback.setSubmittedAt(LocalDate.now());
        feedback.setEvent(event);

        return toDTO(feedbackRepository.save(feedback));
    }

    public List<EventFeedbackDTO> getFeedbacksByEvent(Long eventId) {
        return feedbackRepository.findByEventId(eventId).stream()
                .map(this::toDTO)
                .toList();
    }

    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }

    private EventFeedbackDTO toDTO(EventFeedback feedback) {
        return new EventFeedbackDTO(
                feedback.getId(),
                feedback.getRating(),
                feedback.getComment(),
                feedback.getSubmittedAt(),
                feedback.getEvent() != null ? feedback.getEvent().getId() : null
        );
    }
}
