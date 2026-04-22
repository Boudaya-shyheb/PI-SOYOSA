package org.example.pi_events.controller;

import lombok.RequiredArgsConstructor;
import org.example.pi_events.DTO.EventCategoryDTO;
import org.example.pi_events.service.EventCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EventCategoryController {

    private final EventCategoryService categoryService;

    @PostMapping
    public EventCategoryDTO createCategory(
            @RequestBody EventCategoryDTO dto,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "ADMIN", "PRESIDENT", "VICE_PRESIDENT");
        return categoryService.createCategory(dto);
    }

    @GetMapping
    public List<EventCategoryDTO> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public EventCategoryDTO getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @PutMapping("/{id}")
    public EventCategoryDTO updateCategory(
            @PathVariable Long id,
            @RequestBody EventCategoryDTO dto,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "ADMIN", "PRESIDENT", "VICE_PRESIDENT");
        return categoryService.updateCategory(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(
            @PathVariable Long id,
            @RequestHeader(value = "User-Role", required = false) String role) {
        RoleSecurity.requireAnyRole(role, "ADMIN", "PRESIDENT");
        categoryService.deleteCategory(id);
    }
}
