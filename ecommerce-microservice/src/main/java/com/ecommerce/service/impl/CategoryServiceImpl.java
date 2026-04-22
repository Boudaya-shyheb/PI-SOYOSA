package com.ecommerce.service.impl;

import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.dto.CreateCategoryRequest;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.EntityMapper;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final EntityMapper entityMapper;
    
    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        log.debug("Fetching category with id: {}", id);
        return categoryRepository.findById(id)
            .map(entityMapper::toCategoryDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        log.debug("Fetching all categories");
        return categoryRepository.findAll().stream()
            .map(entityMapper::toCategoryDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryDTO> getAllCategoriesPaginated(Pageable pageable) {
        log.debug("Fetching all categories with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Category> page = categoryRepository.findAll(pageable);
        return buildPageResponse(page);
    }
    
    @Override
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        log.debug("Creating category with name: {}", request.getName());
        
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("Category with name '" + request.getName() + "' already exists");
        }
        
        Category category = entityMapper.toCategory(request);
        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", saved.getId());
        return entityMapper.toCategoryDTO(saved);
    }
    
    @Override
    public CategoryDTO updateCategory(Long id, CreateCategoryRequest request) {
        log.debug("Updating category with id: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        if (!category.getName().equalsIgnoreCase(request.getName()) &&
            categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("Category with name '" + request.getName() + "' already exists");
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        Category updated = categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", id);
        return entityMapper.toCategoryDTO(updated);
    }
    
    @Override
    public void deleteCategory(Long id) {
        log.debug("Deleting category with id: {}", id);
        
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        if (!category.getProducts().isEmpty()) {
            throw new BusinessException("Cannot delete category with existing products");
        }
        
        categoryRepository.deleteById(id);
        log.info("Category deleted successfully with id: {}", id);
    }

    // ==================== HELPER METHODS ====================

    private PageResponse<CategoryDTO> buildPageResponse(Page<Category> page) {
        return PageResponse.<CategoryDTO>builder()
            .content(page.getContent().stream()
                .map(entityMapper::toCategoryDTO)
                .collect(Collectors.toList()))
            .currentPage(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .isFirst(page.isFirst())
            .isLast(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
