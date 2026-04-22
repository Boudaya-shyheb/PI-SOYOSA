package org.example.pi_events.service;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.EventDTO;
import org.example.pi_events.entity.Event;
import org.example.pi_events.entity.EventCategory;
import org.example.pi_events.repository.EventCategoryRepository;
import org.example.pi_events.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository categoryRepository;

    public EventDTO createEvent(EventDTO eventDTO) {
        Long categoryId = eventDTO.getCategoryId();
        if (categoryId == null) {
            throw new RuntimeException("Category id is required");
        }

        EventCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Event event = new Event();
        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setDate(eventDTO.getDate());
        event.setStartTime(eventDTO.getStartTime());
        event.setEndTime(eventDTO.getEndTime());
        event.setLocation(eventDTO.getLocation());
        event.setMaxParticipants(eventDTO.getMaxParticipants());
        event.setCreatedAt(LocalDateTime.now());
        event.setCategory(category);

        return toDTO(eventRepository.save(event));
    }

    public List<EventDTO> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public EventDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return toDTO(event);
    }

    public EventDTO updateEvent(Long id, EventDTO eventDTO) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        event.setTitle(eventDTO.getTitle());
        event.setDescription(eventDTO.getDescription());
        event.setDate(eventDTO.getDate());
        event.setStartTime(eventDTO.getStartTime());
        event.setEndTime(eventDTO.getEndTime());
        event.setLocation(eventDTO.getLocation());
        event.setMaxParticipants(eventDTO.getMaxParticipants());

        Long categoryId = eventDTO.getCategoryId();
        if (categoryId != null) {
            EventCategory category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            event.setCategory(category);
        }

        return toDTO(eventRepository.save(event));
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    private EventDTO toDTO(Event event) {
        EventCategory category = event.getCategory();
        Long categoryId = category != null ? category.getId() : null;
        String categoryName = category != null ? category.getName() : null;
        return new EventDTO(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getMaxParticipants(),
                event.getCreatedAt(),
                categoryId,
                categoryName
        );
    }

}
