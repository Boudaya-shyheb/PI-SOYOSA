package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.UserServiceFeignService;
import com.ecommerce.service.AuditLogService;
import com.ecommerce.service.SearchLogService;
import com.ecommerce.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/ecommerce/products")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {
    
    private final ProductService productService;
    private final UserServiceFeignService userServiceFeignService;
    private final AuditLogService auditLogService;
    private final SearchLogService searchLogService;
    
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve a list of all products in the catalog")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        log.info("GET /api/ecommerce/products - Fetch all products");
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(new ApiResponse<>("Products retrieved successfully", products));
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get all products with pagination", description = "Retrieve products with pagination support")
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> getAllProductsPaginated(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String direction) {
        log.info("GET /api/ecommerce/products/paginated - Fetch all products with pagination: page={}, size={}", page, size);
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        PageResponse<ProductDTO> result = productService.getAllProductsPaginated(pageable);
        return ResponseEntity.ok(new ApiResponse<>("Products retrieved successfully", result));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        log.info("GET /api/ecommerce/products/{} - Fetch product", id);
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse<>("Product retrieved successfully", product));
    }
    
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieve all products in a specific category")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.info("GET /api/ecommerce/products/category/{} - Fetch products by category", categoryId);
        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(new ApiResponse<>("Products retrieved successfully", products));
    }

    @GetMapping("/category/{categoryId}/paginated")
    @Operation(summary = "Get products by category with pagination", description = "Retrieve products by category with pagination")
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> getProductsByCategoryPaginated(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/ecommerce/products/category/{}/paginated - Fetch products by category with pagination", categoryId);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ProductDTO> result = productService.getProductsByCategoryPaginated(categoryId, pageable);
        return ResponseEntity.ok(new ApiResponse<>("Products retrieved successfully", result));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search for products by keyword")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("GET /api/ecommerce/products/search?keyword={} - Search products", keyword);
        List<ProductDTO> products = productService.searchProducts(keyword);
        searchLogService.logSearch(keyword, "SIMPLE", products.size(), authHeader);
        return ResponseEntity.ok(new ApiResponse<>("Products found: " + products.size(), products));
    }

    @GetMapping("/search/paginated")
    @Operation(summary = "Search products with pagination", description = "Search products with pagination support")
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> searchProductsPaginated(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("GET /api/ecommerce/products/search/paginated?keyword={} - Search products with pagination", keyword);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ProductDTO> result = productService.searchProductsPaginated(keyword, pageable);
        searchLogService.logSearch(keyword, "PAGINATED", (int) result.getTotalElements(), authHeader);
        return ResponseEntity.ok(new ApiResponse<>("Products found: " + result.getTotalElements(), result));
    }

    @PostMapping("/advanced-search")
    @Operation(summary = "Advanced search with filters", description = "Search products with multiple filters: price range, rating, category")
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> advancedSearch(
            @Valid @RequestBody SearchRequest searchRequest,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("POST /api/ecommerce/products/advanced-search - Advanced search with filters");
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ProductDTO> result = productService.advancedSearch(searchRequest, pageable);
        String keyword = searchRequest.getKeyword() != null ? searchRequest.getKeyword() : "advanced";
        searchLogService.logSearch(keyword, "ADVANCED", (int) result.getTotalElements(), authHeader);
        return ResponseEntity.ok(new ApiResponse<>("Search completed. Products found: " + result.getTotalElements(), result));
    }

    @GetMapping("/{id}/recommendations")
    @Operation(summary = "Get product recommendations", description = "Get related products, bestsellers, and trending items")
    public ResponseEntity<ApiResponse<RelatedProductsDTO>> getRecommendations(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        log.info("GET /api/ecommerce/products/{}/recommendations - Get recommendations", id);
        RelatedProductsDTO recommendations = productService.getRecommendations(id);
        return ResponseEntity.ok(new ApiResponse<>("Recommendations retrieved successfully", recommendations));
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "Get related products", description = "Get products from the same category")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getRelatedProducts(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "5") int limit) {
        log.info("GET /api/ecommerce/products/{}/related - Get related products", id);
        Pageable pageable = PageRequest.of(0, limit);
        List<ProductDTO> related = productService.getRelatedProducts(id, pageable);
        return ResponseEntity.ok(new ApiResponse<>("Related products retrieved successfully", related));
    }

    @GetMapping("/{id}/insights")
    @Operation(summary = "Get external insights for a product", description = "Retrieve external insights from third-party sources")
    public ResponseEntity<ApiResponse<List<ExternalProductInsightDTO>>> getProductInsights(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("GET /api/ecommerce/products/{}/insights - Get external insights", id);
        requireAdmin(authHeader);
        List<ExternalProductInsightDTO> insights = productService.getExternalInsights(id);
        return ResponseEntity.ok(new ApiResponse<>("External insights retrieved successfully", insights));
    }

    @GetMapping("/insights/summary")
    @Operation(summary = "Get external insights summary", description = "Retrieve summary counts for external insights")
    public ResponseEntity<ApiResponse<ExternalInsightsSummaryDTO>> getInsightsSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("GET /api/ecommerce/products/insights/summary - Get external insights summary");
        requireAdmin(authHeader);
        ExternalInsightsSummaryDTO summary = productService.getExternalInsightsSummary();
        return ResponseEntity.ok(new ApiResponse<>("External insights summary retrieved", summary));
    }

    @GetMapping("/insights/catalog")
    @Operation(summary = "Get external insights catalog", description = "List external insight records with product match info")
    public ResponseEntity<ApiResponse<PageResponse<ExternalInsightRecordDTO>>> getInsightsCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("GET /api/ecommerce/products/insights/catalog - Get external insights catalog, page={}, size={}", page, size);
        requireAdmin(authHeader);
        PageResponse<ExternalInsightRecordDTO> records = productService.getExternalInsightsCatalog(page, size);
        return ResponseEntity.ok(new ApiResponse<>("External insights catalog retrieved", records));
    }

    @GetMapping("/bestsellers")
    @Operation(summary = "Get bestselling products", description = "Get most ordered products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getBestsellers(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/ecommerce/products/bestsellers - Get bestsellers");
        Pageable pageable = PageRequest.of(0, limit);
        List<ProductDTO> bestsellers = productService.getBestsellers(pageable);
        return ResponseEntity.ok(new ApiResponse<>("Bestsellers retrieved successfully", bestsellers));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending products", description = "Get most recently added or reviewed products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getTrendingProducts(
            @Parameter(description = "Limit") @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/ecommerce/products/trending - Get trending products");
        Pageable pageable = PageRequest.of(0, limit);
        List<ProductDTO> trending = productService.getTrendingProducts(pageable);
        return ResponseEntity.ok(new ApiResponse<>("Trending products retrieved successfully", trending));
    }

    @PostMapping("/compare")
    @Operation(summary = "Compare products", description = "Compare 2-4 products side by side")
    public ResponseEntity<ApiResponse<ProductComparisonDTO>> compareProducts(
            @Valid @RequestBody ComparisonRequest request) {
        log.info("POST /api/ecommerce/products/compare - Compare products: ids={}", request.getProductIds());
        ProductComparisonDTO comparison = productService.compareProducts(request);
        return ResponseEntity.ok(new ApiResponse<>("Comparison completed successfully", comparison));
    }
    
    @PostMapping
    @Operation(summary = "Create new product", description = "Create a new product in the catalog")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateProductRequest request) {
        log.info("POST /api/ecommerce/products - Create new product: {}", request.getName());
        requireAdmin(authHeader);
        ProductDTO created = productService.createProduct(request);
        auditLogService.logAction(authHeader, "PRODUCT_CREATE", "PRODUCT", created.getId(),
            "Created product '" + created.getName() + "'");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>("Product created successfully", created));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
        @Parameter(description = "Product ID") @PathVariable Long id,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody CreateProductRequest request) {
        log.info("PUT /api/ecommerce/products/{} - Update product", id);
        requireAdmin(authHeader);
        ProductDTO updated = productService.updateProduct(id, request);
        auditLogService.logAction(authHeader, "PRODUCT_UPDATE", "PRODUCT", id,
            "Updated product '" + updated.getName() + "'");
        return ResponseEntity.ok(new ApiResponse<>("Product updated successfully", updated));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product from the catalog")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("DELETE /api/ecommerce/products/{} - Delete product", id);
        requireAdmin(authHeader);
        productService.deleteProduct(id);
        auditLogService.logAction(authHeader, "PRODUCT_DELETE", "PRODUCT", id,
            "Deleted product id=" + id);
        return ResponseEntity.ok(new ApiResponse<>("Product deleted successfully", null));
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
