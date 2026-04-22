package com.ecommerce.service;

import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.dto.CreateCategoryRequest;
import com.ecommerce.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryDTO getCategoryById(Long id);
    List<CategoryDTO> getAllCategories();
    PageResponse<CategoryDTO> getAllCategoriesPaginated(Pageable pageable);
    CategoryDTO createCategory(CreateCategoryRequest request);
    CategoryDTO updateCategory(Long id, CreateCategoryRequest request);
    void deleteCategory(Long id);
}
