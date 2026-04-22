package org.example.pi_events.service;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.EventCategoryDTO;
import org.example.pi_events.entity.EventCategory;
import org.example.pi_events.repository.EventCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventCategoryService {

    private final EventCategoryRepository eventCategoryRepository;

    public EventCategoryDTO createCategory(EventCategoryDTO categoryDTO) {
        EventCategory category = new EventCategory();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        return toDTO(eventCategoryRepository.save(category));
    }

    public List<EventCategoryDTO> getAllCategories() {
        return eventCategoryRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public EventCategoryDTO getCategoryById(Long id) {
        EventCategory category = eventCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return toDTO(category);
    }

    public EventCategoryDTO updateCategory(Long id, EventCategoryDTO categoryDTO) {
        EventCategory category = eventCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        return toDTO(eventCategoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        eventCategoryRepository.deleteById(id);
    }

    private EventCategoryDTO toDTO(EventCategory category) {
        return new EventCategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
