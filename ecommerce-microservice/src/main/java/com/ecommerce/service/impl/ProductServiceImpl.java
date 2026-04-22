package com.ecommerce.service.impl;

import com.ecommerce.dto.*;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.ExternalProductInsight;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.EntityMapper;
import com.ecommerce.repository.ExternalProductInsightRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {

    private static final double RECO_WEIGHT_CATEGORY = 0.45;
    private static final double RECO_WEIGHT_RATING = 0.35;
    private static final double RECO_WEIGHT_RECENCY = 0.10;
    private static final double RECO_WEIGHT_EXTERNAL = 0.10;
    private static final double RECO_RECENCY_WINDOW_DAYS = 30.0;
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ExternalProductInsightRepository externalProductInsightRepository;
    private final EntityMapper entityMapper;
    
    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product with id: {}", id);
        return productRepository.findById(id)
            .map(this::applyDiscountSnapshot)
            .map(entityMapper::toProductDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.debug("Fetching all products");
        return productRepository.findAll().stream()
            .map(this::applyDiscountSnapshot)
            .map(entityMapper::toProductDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> getAllProductsPaginated(Pageable pageable) {
        log.debug("Fetching all products with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> page = productRepository.findAll(pageable);
        return buildPageResponse(page);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        log.debug("Fetching products by category: {}", categoryId);
        
        // Verify category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }
        
        return productRepository.findByCategoryId(categoryId).stream()
            .map(this::applyDiscountSnapshot)
            .map(entityMapper::toProductDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> getProductsByCategoryPaginated(Long categoryId, Pageable pageable) {
        log.debug("Fetching products by category with pagination: categoryId={}, page={}, size={}", 
                 categoryId, pageable.getPageNumber(), pageable.getPageSize());
        
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", "id", categoryId);
        }
        
        Page<Product> page = productRepository.findByCategoryId(categoryId, pageable);
        return buildPageResponse(page);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword) {
        log.debug("Searching products with keyword: {}", keyword);
        return productRepository.findByNameContainingIgnoreCase(keyword).stream()
            .map(this::applyDiscountSnapshot)
            .map(entityMapper::toProductDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> searchProductsPaginated(String keyword, Pageable pageable) {
        log.debug("Searching products with pagination: keyword={}, page={}, size={}", 
                 keyword, pageable.getPageNumber(), pageable.getPageSize());
        Page<Product> page = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return buildPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDTO> advancedSearch(SearchRequest searchRequest, Pageable pageable) {
        log.debug("Advanced search with filters: keyword={}, minPrice={}, maxPrice={}, categoryId={}, minRating={}", 
                 searchRequest.getKeyword(), searchRequest.getMinPrice(), searchRequest.getMaxPrice(),
                 searchRequest.getCategoryId(), searchRequest.getMinRating());
        
        Page<Product> page = productRepository.searchProducts(
            searchRequest.getKeyword(),
            searchRequest.getMinPrice(),
            searchRequest.getMaxPrice(),
            searchRequest.getCategoryId(),
            searchRequest.getMinRating() != null ? java.math.BigDecimal.valueOf(searchRequest.getMinRating()) : null,
            pageable
        );
        
        return buildPageResponse(page);
    }
    
    @Override
    public ProductDTO createProduct(CreateProductRequest request) {
        log.debug("Creating product with name: {}", request.getName());
        
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("Product with name '" + request.getName() + "' already exists");
        }
        
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (request.getOriginalPrice() != null && request.getOriginalPrice().compareTo(request.getPrice()) < 0) {
            throw new BusinessException("Original price must be greater than or equal to price");
        }
        
        Product product = entityMapper.toProduct(request);
        product.setIsbn(normalizeIsbn(request.getIsbn()));
        product.setCategory(category);
        applyDiscountFromRequest(product, request);
        
        Product saved = productRepository.save(product);
        log.info("Product created successfully with id: {}", saved.getId());
        return entityMapper.toProductDTO(saved);
    }
    
    @Override
    public ProductDTO updateProduct(Long id, CreateProductRequest request) {
        log.debug("Updating product with id: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        if (!product.getName().equalsIgnoreCase(request.getName()) &&
            productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("Product with name '" + request.getName() + "' already exists");
        }
        
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        
        product.setName(request.getName());
        product.setIsbn(normalizeIsbn(request.getIsbn()));
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        applyDiscountFromRequest(product, request);
        product.setQuantityAvailable(request.getQuantityAvailable());
        product.setCategory(category);
        
        Product updated = productRepository.save(product);
        log.info("Product updated successfully with id: {}", id);
        return entityMapper.toProductDTO(updated);
    }
    
    @Override
    public void deleteProduct(Long id) {
        log.debug("Deleting product with id: {}", id);
        
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        
        productRepository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }
    
    @Override
    public void decreaseStock(Long productId, Integer quantity) {
        log.debug("Decreasing stock for product {} by {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        if (product.getQuantityAvailable() < quantity) {
            throw new BusinessException("Insufficient stock for product: " + product.getName());
        }
        
        product.setQuantityAvailable(product.getQuantityAvailable() - quantity);
        productRepository.save(product);
        log.info("Stock decreased for product {} by {}", productId, quantity);
    }

    // ==================== RECOMMENDATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public RelatedProductsDTO getRecommendations(Long productId) {
        log.debug("Getting recommendations for product: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        Pageable limit5 = PageRequest.of(0, 5);
        Pageable limit20 = PageRequest.of(0, 20);

        List<ProductDTO> relatedProducts = rankProducts(
            productRepository.findRelatedProducts(productId, product.getCategory().getId(), limit20),
            product.getCategory().getId(),
            limit5.getPageSize()
        );
        List<ProductDTO> bestSellers = rankProducts(
            productRepository.findBestsellers(limit20),
            null,
            limit5.getPageSize()
        );
        List<ProductDTO> trending = rankProducts(
            productRepository.findTrending(limit20),
            null,
            limit5.getPageSize()
        );
        
        return RelatedProductsDTO.builder()
            .relatedProducts(relatedProducts)
            .bestSellers(bestSellers)
            .trending(trending)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getRelatedProducts(Long productId, Pageable pageable) {
        log.debug("Getting related products for product: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        return rankProducts(
            productRepository.findRelatedProducts(productId, product.getCategory().getId(), pageable),
            product.getCategory().getId(),
            pageable.getPageSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getBestsellers(Pageable pageable) {
        log.debug("Getting bestseller products");
        return rankProducts(productRepository.findBestsellers(pageable), null, pageable.getPageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getTrendingProducts(Pageable pageable) {
        log.debug("Getting trending products");
        return rankProducts(productRepository.findTrending(pageable), null, pageable.getPageSize());
    }

    // ==================== COMPARISON ====================

    @Override
    @Transactional(readOnly = true)
    public ProductComparisonDTO compareProducts(ComparisonRequest request) {
        log.debug("Comparing products: ids={}", request.getProductIds());
        
        if (request.getProductIds().isEmpty() || request.getProductIds().size() < 2) {
            throw new BusinessException("At least 2 products are required for comparison");
        }
        
        if (request.getProductIds().size() > 4) {
            throw new BusinessException("Maximum 4 products can be compared");
        }
        
        List<Product> products = productRepository.findByIdIn(request.getProductIds().stream().toList());
        
        if (products.size() != request.getProductIds().size()) {
            throw new ResourceNotFoundException("One or more products not found");
        }
        
        List<ProductComparisonDTO.ComparisonItemDTO> items = products.stream()
            .map(this::applyDiscountSnapshot)
            .map(p -> ProductComparisonDTO.ComparisonItemDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .quantityAvailable(p.getQuantityAvailable())
                .averageRating(p.getAverageRating())
                .reviewCount(p.getReviewCount())
                .imageUrl(p.getImageUrl())
                .category(entityMapper.toCategoryDTO(p.getCategory()))
                .inStock(p.getQuantityAvailable() > 0)
                .build())
            .collect(Collectors.toList());
        
        return ProductComparisonDTO.builder()
            .items(items)
            .build();
    }

    // ==================== EXTERNAL INSIGHTS ====================

    @Override
    @Transactional(readOnly = true)
    public List<ExternalProductInsightDTO> getExternalInsights(Long productId) {
        log.debug("Fetching external insights for product {}", productId);
        List<ExternalProductInsight> insights = externalProductInsightRepository.findByProductId(productId);
        return insights.stream()
            .sorted(Comparator.comparing(ExternalProductInsight::getSource))
            .map(this::toExternalInsightDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExternalInsightsSummaryDTO getExternalInsightsSummary() {
        long totalProducts = externalProductInsightRepository.countDistinctProducts();
        long totalRecords = externalProductInsightRepository.count();
        long openAlexCount = externalProductInsightRepository.countBySource("openalex");
        long crossrefCount = externalProductInsightRepository.countBySource("crossref");
        LocalDateTime lastUpdatedAt = externalProductInsightRepository.findLastUpdatedAt();
        return ExternalInsightsSummaryDTO.builder()
            .productsWithInsights(totalProducts)
            .totalRecords(totalRecords)
            .openAlexCount(openAlexCount)
            .crossrefCount(crossrefCount)
            .lastUpdatedAt(lastUpdatedAt)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ExternalInsightRecordDTO> getExternalInsightsCatalog(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 200));
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<ExternalProductInsight> insightPage = externalProductInsightRepository.findAllWithProduct(pageable);
        List<ExternalInsightRecordDTO> content = insightPage.getContent().stream()
            .map(this::toExternalInsightRecordDTO)
            .collect(Collectors.toList());
        return PageResponse.<ExternalInsightRecordDTO>builder()
            .content(content)
            .currentPage(insightPage.getNumber())
            .pageSize(insightPage.getSize())
            .totalElements(insightPage.getTotalElements())
            .totalPages(insightPage.getTotalPages())
            .isFirst(insightPage.isFirst())
            .isLast(insightPage.isLast())
            .hasNext(insightPage.hasNext())
            .hasPrevious(insightPage.hasPrevious())
            .build();
    }

    // ==================== HELPER METHODS ====================

    private PageResponse<ProductDTO> buildPageResponse(Page<Product> page) {
        return PageResponse.<ProductDTO>builder()
            .content(page.getContent().stream()
                .map(this::applyDiscountSnapshot)
                .map(entityMapper::toProductDTO)
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

    private String normalizeIsbn(String rawIsbn) {
        if (rawIsbn == null) {
            return null;
        }
        String trimmed = rawIsbn.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = trimmed.replace(" ", "").replace("-", "").toUpperCase();
        if (!isValidIsbn(normalized)) {
            throw new BusinessException("ISBN must be a valid ISBN-10 or ISBN-13");
        }
        return normalized;
    }

    private boolean isValidIsbn(String isbn) {
        if (isbn == null) {
            return false;
        }
        if (isbn.length() == 10) {
            return isValidIsbn10(isbn);
        }
        if (isbn.length() == 13) {
            return isValidIsbn13(isbn);
        }
        return false;
    }

    private boolean isValidIsbn10(String isbn) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            char ch = isbn.charAt(i);
            if (ch < '0' || ch > '9') {
                return false;
            }
            sum += (i + 1) * (ch - '0');
        }
        char last = isbn.charAt(9);
        int checkValue;
        if (last == 'X') {
            checkValue = 10;
        } else if (last >= '0' && last <= '9') {
            checkValue = last - '0';
        } else {
            return false;
        }
        sum += 10 * checkValue;
        return sum % 11 == 0;
    }

    private boolean isValidIsbn13(String isbn) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            char ch = isbn.charAt(i);
            if (ch < '0' || ch > '9') {
                return false;
            }
            int digit = ch - '0';
            sum += (i % 2 == 0) ? digit : (digit * 3);
        }
        char last = isbn.charAt(12);
        if (last < '0' || last > '9') {
            return false;
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit == (last - '0');
    }

    private void applyDiscountFromRequest(Product product, CreateProductRequest request) {
        BigDecimal price = request.getPrice();
        BigDecimal originalPrice = request.getOriginalPrice();
        BigDecimal discountPercent = request.getDiscountPercent();
        LocalDateTime discountEndsAt = request.getDiscountEndsAt();

        if (discountPercent != null) {
            if (discountPercent.compareTo(BigDecimal.ZERO) <= 0 || discountPercent.compareTo(new BigDecimal("90")) > 0) {
                throw new BusinessException("Discount percent must be between 0 and 90");
            }
            if (originalPrice == null) {
                originalPrice = price;
            }
            BigDecimal multiplier = BigDecimal.ONE.subtract(discountPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            price = originalPrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
            product.setPrice(price);
            product.setOriginalPrice(originalPrice);
            product.setDiscountPercent(discountPercent);
            product.setDiscountEndsAt(discountEndsAt);
        } else {
            if (originalPrice != null && originalPrice.compareTo(price) < 0) {
                throw new BusinessException("Original price must be greater than or equal to price");
            }
            product.setPrice(price);
            product.setOriginalPrice(originalPrice);
            product.setDiscountPercent(null);
            product.setDiscountEndsAt(discountEndsAt);
        }

        if (discountEndsAt != null && discountEndsAt.isBefore(LocalDateTime.now())) {
            clearExpiredDiscount(product);
        }
    }

    private void clearExpiredDiscount(Product product) {
        if (product.getDiscountEndsAt() == null) {
            return;
        }
        if (product.getDiscountEndsAt().isAfter(LocalDateTime.now())) {
            return;
        }
        BigDecimal fallbackPrice = product.getOriginalPrice() != null ? product.getOriginalPrice() : product.getPrice();
        product.setPrice(fallbackPrice);
        product.setOriginalPrice(null);
        product.setDiscountPercent(null);
        product.setDiscountEndsAt(null);
    }

    private Product applyDiscountSnapshot(Product product) {
        if (product.getDiscountEndsAt() == null) {
            return product;
        }
        if (product.getDiscountEndsAt().isAfter(LocalDateTime.now())) {
            return product;
        }
        Product snapshot = Product.builder()
            .name(product.getName())
            .isbn(product.getIsbn())
            .description(product.getDescription())
            .imageUrl(product.getImageUrl())
            .price(product.getOriginalPrice() != null ? product.getOriginalPrice() : product.getPrice())
            .originalPrice(null)
            .discountPercent(null)
            .discountEndsAt(null)
            .quantityAvailable(product.getQuantityAvailable())
            .averageRating(product.getAverageRating())
            .reviewCount(product.getReviewCount())
            .category(product.getCategory())
            .build();
        snapshot.setId(product.getId());
        snapshot.setCreatedAt(product.getCreatedAt());
        snapshot.setUpdatedAt(product.getUpdatedAt());
        return snapshot;
    }

    private List<ProductDTO> rankProducts(List<Product> products, Long categoryId, int limit) {
        LocalDateTime now = LocalDateTime.now();
        Map<Long, Double> externalRatings = buildExternalRatingMap(products);
        return products.stream()
            .map(product -> new ScoredProduct(
                product,
                scoreProduct(product, categoryId, now, externalRatings.get(product.getId()))
            ))
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .limit(limit)
            .map(scored -> applyDiscountSnapshot(scored.product))
            .map(entityMapper::toProductDTO)
            .collect(Collectors.toList());
    }

    private Map<Long, Double> buildExternalRatingMap(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        List<Long> productIds = products.stream()
            .map(Product::getId)
            .filter(id -> id != null)
            .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        List<ExternalProductInsight> insights = externalProductInsightRepository.findByProductIdIn(productIds);
        Map<Long, Double> ratings = new HashMap<>();
        for (ExternalProductInsight insight : insights) {
            if (insight.getProduct() == null || insight.getProduct().getId() == null) {
                continue;
            }
            if (insight.getAverageRating() == null) {
                continue;
            }
            Long productId = insight.getProduct().getId();
            double rating = insight.getAverageRating().doubleValue();
            ratings.merge(productId, rating, Math::max);
        }
        return ratings;
    }

    private double scoreProduct(Product product, Long categoryId, LocalDateTime now, Double externalRating) {
        double categoryMatch = 0.0;
        if (categoryId != null && product.getCategory() != null && product.getCategory().getId() != null) {
            categoryMatch = product.getCategory().getId().equals(categoryId) ? 1.0 : 0.0;
        }

        double ratingScore = 0.0;
        if (product.getAverageRating() != null) {
            ratingScore = product.getAverageRating().doubleValue() / 5.0;
        }

        double recencyScore = 0.0;
        if (product.getCreatedAt() != null) {
            long ageDays = java.time.Duration.between(product.getCreatedAt(), now).toDays();
            recencyScore = Math.max(0.0, 1.0 - (ageDays / RECO_RECENCY_WINDOW_DAYS));
        }

        double externalScore = 0.0;
        if (externalRating != null) {
            externalScore = Math.min(1.0, Math.max(0.0, externalRating / 5.0));
        }

        return (RECO_WEIGHT_CATEGORY * categoryMatch)
            + (RECO_WEIGHT_RATING * ratingScore)
            + (RECO_WEIGHT_RECENCY * recencyScore)
            + (RECO_WEIGHT_EXTERNAL * externalScore);
    }

    private static class ScoredProduct {
        private final Product product;
        private final double score;

        private ScoredProduct(Product product, double score) {
            this.product = product;
            this.score = score;
        }
    }

    private ExternalProductInsightDTO toExternalInsightDTO(ExternalProductInsight insight) {
        return ExternalProductInsightDTO.builder()
            .productId(insight.getProduct() != null ? insight.getProduct().getId() : null)
            .source(insight.getSource())
            .sourceId(insight.getSourceId())
            .isbn(insight.getIsbn())
            .title(insight.getTitle())
            .authors(insight.getAuthors())
            .publisher(insight.getPublisher())
            .publishedDate(insight.getPublishedDate())
            .pageCount(insight.getPageCount())
            .language(insight.getLanguage())
            .categories(insight.getCategories())
            .averageRating(insight.getAverageRating())
            .ratingsCount(insight.getRatingsCount())
            .thumbnailUrl(insight.getThumbnailUrl())
            .infoLink(insight.getInfoLink())
            .updatedAt(insight.getUpdatedAt())
            .build();
    }

    private ExternalInsightRecordDTO toExternalInsightRecordDTO(ExternalProductInsight insight) {
        Product product = insight.getProduct();
        boolean matched = isLikelyMatch(product, insight);
        return ExternalInsightRecordDTO.builder()
            .id(insight.getId())
            .source(insight.getSource())
            .sourceId(insight.getSourceId())
            .isbn(insight.getIsbn())
            .title(insight.getTitle())
            .authors(insight.getAuthors())
            .publisher(insight.getPublisher())
            .publishedDate(insight.getPublishedDate())
            .pageCount(insight.getPageCount())
            .language(insight.getLanguage())
            .categories(insight.getCategories())
            .averageRating(insight.getAverageRating())
            .ratingsCount(insight.getRatingsCount())
            .infoLink(insight.getInfoLink())
            .updatedAt(insight.getUpdatedAt())
            .matched(matched)
            .productId(matched ? product.getId() : null)
            .productName(matched ? product.getName() : null)
            .productIsbn(matched ? product.getIsbn() : null)
            .productPrice(matched ? product.getPrice() : null)
            .productCategoryName(matched && product.getCategory() != null ? product.getCategory().getName() : null)
            .productAverageRating(matched ? product.getAverageRating() : null)
            .productReviewCount(matched ? product.getReviewCount() : null)
            .build();
    }

    private boolean isLikelyMatch(Product product, ExternalProductInsight insight) {
        if (product == null || insight == null) {
            return false;
        }
        String productIsbn = normalizeIsbnLoose(product.getIsbn());
        String insightIsbn = normalizeIsbnLoose(insight.getIsbn());
        if (productIsbn != null && insightIsbn != null) {
            return productIsbn.equals(insightIsbn);
        }

        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        boolean isBook = categoryName != null && categoryName.equalsIgnoreCase("Books");
        double titleScore = textSimilarity(product.getName(), insight.getTitle());
        return isBook ? titleScore >= 0.75 : titleScore >= 0.90;
    }

    private String normalizeIsbnLoose(String rawIsbn) {
        if (rawIsbn == null) {
            return null;
        }
        String trimmed = rawIsbn.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.replace(" ", "").replace("-", "").toUpperCase();
    }

    private double textSimilarity(String left, String right) {
        if (left == null || right == null) {
            return 0.0;
        }
        Set<String> leftTokens = tokenize(left);
        Set<String> rightTokens = tokenize(right);
        if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
            return 0.0;
        }
        int overlap = 0;
        for (String token : leftTokens) {
            if (rightTokens.contains(token)) {
                overlap += 1;
            }
        }
        int denom = Math.max(leftTokens.size(), rightTokens.size());
        return denom == 0 ? 0.0 : (double) overlap / (double) denom;
    }

    private Set<String> tokenize(String value) {
        String normalized = value.toLowerCase().replaceAll("[^a-z0-9]+", " ").trim();
        if (normalized.isEmpty()) {
            return java.util.Collections.emptySet();
        }
        Set<String> tokens = new java.util.HashSet<>();
        for (String token : normalized.split("\\s+")) {
            if (token.length() < 2) {
                continue;
            }
            if (isStopWord(token)) {
                continue;
            }
            tokens.add(token);
        }
        return tokens;
    }

    private boolean isStopWord(String token) {
        return token.equals("the")
            || token.equals("and")
            || token.equals("for")
            || token.equals("with")
            || token.equals("from")
            || token.equals("into")
            || token.equals("that")
            || token.equals("this")
            || token.equals("your")
            || token.equals("you")
            || token.equals("our")
            || token.equals("are")
            || token.equals("was")
            || token.equals("were")
            || token.equals("book")
            || token.equals("books");
    }
}