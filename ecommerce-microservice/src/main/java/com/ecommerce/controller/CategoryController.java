package com.ecommerce.controller;

import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.dto.CreateCategoryRequest;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.UserServiceFeignService;
import com.ecommerce.service.AuditLogService;
import com.ecommerce.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ecommerce/categories")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {
    
    private final CategoryService categoryService;
    private final UserServiceFeignService userServiceFeignService;
    private final AuditLogService auditLogService;
    
    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve a list of all product categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        log.info("GET /api/ecommerce/categories - Fetch all categories");
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(new ApiResponse<>("Categories retrieved successfully", categories));
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all categories with pagination", description = "Retrieve categories with pagination support")
    public ResponseEntity<ApiResponse<PageResponse<CategoryDTO>>> getAllCategoriesPaginated(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String direction) {
        log.info("GET /api/ecommerce/categories/paginated - Fetch all categories with pagination");
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        PageResponse<CategoryDTO> result = categoryService.getAllCategoriesPaginated(pageable);
        return ResponseEntity.ok(new ApiResponse<>("Categories retrieved successfully", result));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.info("GET /api/ecommerce/categories/{} - Fetch category", id);
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(new ApiResponse<>("Category retrieved successfully", category));
    }
    
    @PostMapping
    @Operation(summary = "Create new category", description = "Create a new product category")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody CreateCategoryRequest request) {
        log.info("POST /api/ecommerce/categories - Create new category: {}", request.getName());
        requireAdmin(authHeader);
        CategoryDTO created = categoryService.createCategory(request);
        auditLogService.logAction(authHeader, "CATEGORY_CREATE", "CATEGORY", created.getId(),
            "Created category '" + created.getName() + "'");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>("Category created successfully", created));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update an existing product category")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
        @Parameter(description = "Category ID") @PathVariable Long id,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody CreateCategoryRequest request) {
        log.info("PUT /api/ecommerce/categories/{} - Update category", id);
        requireAdmin(authHeader);
        CategoryDTO updated = categoryService.updateCategory(id, request);
        auditLogService.logAction(authHeader, "CATEGORY_UPDATE", "CATEGORY", id,
            "Updated category '" + updated.getName() + "'");
        return ResponseEntity.ok(new ApiResponse<>("Category updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a product category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("DELETE /api/ecommerce/categories/{} - Delete category", id);
        requireAdmin(authHeader);
        categoryService.deleteCategory(id);
        auditLogService.logAction(authHeader, "CATEGORY_DELETE", "CATEGORY", id,
            "Deleted category id=" + id);
        return ResponseEntity.ok(new ApiResponse<>("Category deleted successfully", null));
    }

    private void requireValidToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new BusinessException("Authorization header required");
        }
        if (!userServiceFeignService.validateToken(authHeader)) {
            throw new BusinessException("Invalid or expired token");
        }
    }

    private void requireAdmin(String authHeader) {
        requireValidToken(authHeader);
        String role = userServiceFeignService.getUserRole(authHeader);
        if (role == null || role.isBlank()) {
            throw new BusinessException("Invalid user role");
        }
        if (!"ADMIN".equals(role)) {
            throw new BusinessException("Admin privileges required");
        }
    }
}
