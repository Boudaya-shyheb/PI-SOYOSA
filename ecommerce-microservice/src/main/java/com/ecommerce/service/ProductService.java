package com.ecommerce.service;

import com.ecommerce.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductDTO getProductById(Long id);
    List<ProductDTO> getAllProducts();
    PageResponse<ProductDTO> getAllProductsPaginated(Pageable pageable);
    List<ProductDTO> getProductsByCategory(Long categoryId);
    PageResponse<ProductDTO> getProductsByCategoryPaginated(Long categoryId, Pageable pageable);
    List<ProductDTO> searchProducts(String keyword);
    PageResponse<ProductDTO> searchProductsPaginated(String keyword, Pageable pageable);
    PageResponse<ProductDTO> advancedSearch(SearchRequest searchRequest, Pageable pageable);
    ProductDTO createProduct(CreateProductRequest request);
    ProductDTO updateProduct(Long id, CreateProductRequest request);
    void deleteProduct(Long id);
    void decreaseStock(Long productId, Integer quantity);
    
    // Recommendations
    RelatedProductsDTO getRecommendations(Long productId);
    List<ProductDTO> getRelatedProducts(Long productId, Pageable pageable);
    List<ProductDTO> getBestsellers(Pageable pageable);
    List<ProductDTO> getTrendingProducts(Pageable pageable);
    
    // Comparison
    ProductComparisonDTO compareProducts(ComparisonRequest request);

    // External insights
    List<ExternalProductInsightDTO> getExternalInsights(Long productId);
    ExternalInsightsSummaryDTO getExternalInsightsSummary();
    PageResponse<ExternalInsightRecordDTO> getExternalInsightsCatalog(int page, int size);
}
