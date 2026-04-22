package org.example.pi_events.service;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.EventParticipationDTO;
import org.example.pi_events.entity.Event;
import org.example.pi_events.entity.EventParticipation;
import org.example.pi_events.repository.EventParticipationRepository;
import org.example.pi_events.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventParticipationService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final EventParticipationRepository participationRepository;
    private final EventRepository eventRepository;

    public EventParticipationDTO participate(Long eventId, EventParticipationDTO dto, String userEmail) {
        String normalizedEmail = normalizeEmail(userEmail, dto.getEmail());

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        EventParticipation participation = participationRepository.findByEventIdAndEmail(eventId, normalizedEmail)
                .orElseGet(EventParticipation::new);

        if (participation.getId() != null) {
            if (STATUS_PENDING.equals(participation.getStatus())) {
                throw new RuntimeException("Your participation request is already pending admin approval");
            }
            if (STATUS_APPROVED.equals(participation.getStatus())) {
                throw new RuntimeException("You are already participating in this event");
            }
        }

        participation.setParticipationDate(LocalDate.now());
        participation.setStatus(STATUS_PENDING);

        participation.setFullName(buildFullName(dto.getFullName(), normalizedEmail));
        participation.setEmail(normalizedEmail);
        participation.setPhone(dto.getPhone());
        participation.setUniversity(dto.getUniversity());
        participation.setLevel(dto.getLevel());
        participation.setMotivation(dto.getMotivation());
        participation.setEvent(event);

        return toDTO(participationRepository.save(participation));
    }

    public List<EventParticipationDTO> getParticipationsByEvent(Long eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        return participationRepository.findByEventId(eventId)
                .stream()
                .filter(this::isVisibleParticipant)
                .sorted(Comparator.comparing(EventParticipation::getParticipationDate).reversed())
                .map(this::toDTO)
                .toList();
    }

    public List<EventParticipationDTO> getPendingRequestsByEvent(Long eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        return participationRepository.findByEventIdAndStatusOrderByParticipationDateDesc(eventId, STATUS_PENDING)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<EventParticipationDTO> getMyParticipationRequests(String userEmail) {
        return participationRepository.findByEmailOrderByParticipationDateDesc(normalizeEmail(userEmail, null))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public EventParticipationDTO approveParticipationRequest(Long participationId) {
        EventParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation request not found"));

        if (!STATUS_PENDING.equals(participation.getStatus())) {
            throw new RuntimeException("Only pending requests can be approved");
        }

        Event event = participation.getEvent();
        Long eventId = event.getId();
        if (event.getMaxParticipants() != null) {
            long currentApproved = participationRepository.findByEventId(eventId)
                    .stream()
                    .filter(this::isVisibleParticipant)
                    .count();
            if (currentApproved >= event.getMaxParticipants()) {
                throw new RuntimeException("Event is full");
            }
        }

        participation.setStatus(STATUS_APPROVED);
        return toDTO(participationRepository.save(participation));
    }

    public EventParticipationDTO rejectParticipationRequest(Long participationId) {
        EventParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation request not found"));

        if (!STATUS_PENDING.equals(participation.getStatus())) {
            throw new RuntimeException("Only pending requests can be rejected");
        }

        participation.setStatus(STATUS_REJECTED);
        return toDTO(participationRepository.save(participation));
    }

    public void cancelParticipation(Long id) {
        participationRepository.deleteById(id);
    }

    private EventParticipationDTO toDTO(EventParticipation participation) {
        return new EventParticipationDTO(
                participation.getId(),
                participation.getParticipationDate(),
                participation.getStatus(),
                participation.getFullName(),
                participation.getEmail(),
                participation.getPhone(),
                participation.getUniversity(),
                participation.getLevel(),
                participation.getMotivation(),
                participation.getEvent() != null ? participation.getEvent().getId() : null
        );
    }
    // 🔥 GET PARTICIPATION BY ID
    public EventParticipationDTO getParticipationById(Long id) {

        EventParticipation participation = participationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participation not found"));

        return toDTO(participation);
    }

    private String buildFullName(String fullName, String email) {
        if (fullName != null && !fullName.isBlank()) {
            return fullName.trim();
        }
        String fallbackEmail = normalizeEmail(email, null);
        int atIndex = fallbackEmail.indexOf('@');
        if (atIndex > 0) {
            return fallbackEmail.substring(0, atIndex);
        }
        return fallbackEmail;
    }

    private String normalizeEmail(String userEmail, String dtoEmail) {
        String raw = (userEmail != null && !userEmail.isBlank()) ? userEmail : dtoEmail;
        if (raw == null || raw.isBlank()) {
            return "guest@local";
        }
        return raw.trim().toLowerCase();
    }

    private boolean isVisibleParticipant(EventParticipation participation) {
        String status = participation.getStatus();
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return !STATUS_PENDING.equals(normalized) && !STATUS_REJECTED.equals(normalized);
    }
}
