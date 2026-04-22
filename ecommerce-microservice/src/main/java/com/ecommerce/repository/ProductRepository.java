package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Basic queries
    List<Product> findByCategoryId(Long categoryId);
    Optional<Product> findByNameIgnoreCase(String name);
    List<Product> findByNameContainingIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    // Pagination support
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Advanced search filters
    @Query("SELECT p FROM Product p WHERE " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:minRating IS NULL OR p.averageRating >= :minRating)")
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("categoryId") Long categoryId,
            @Param("minRating") BigDecimal minRating,
            Pageable pageable);

    // Recommendations
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id != :productId ORDER BY p.averageRating DESC")
    List<Product> findRelatedProducts(@Param("productId") Long productId, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY p.reviewCount DESC")
    List<Product> findBestsellers(Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findTrending(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findByIdIn(@Param("ids") List<Long> ids);
}
