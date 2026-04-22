package com.ecommerce.repository;

import com.ecommerce.entity.ExternalProductInsight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface ExternalProductInsightRepository extends JpaRepository<ExternalProductInsight, Long> {
    Optional<ExternalProductInsight> findByProductIdAndSource(Long productId, String source);
    List<ExternalProductInsight> findByProductIdIn(List<Long> productIds);
    List<ExternalProductInsight> findByProductId(Long productId);

    @Query(
        value = "SELECT e FROM ExternalProductInsight e LEFT JOIN FETCH e.product ORDER BY e.updatedAt DESC",
        countQuery = "SELECT COUNT(e) FROM ExternalProductInsight e"
    )
    Page<ExternalProductInsight> findAllWithProduct(Pageable pageable);

    @Query("SELECT COUNT(DISTINCT e.product.id) FROM ExternalProductInsight e")
    long countDistinctProducts();

    @Query("SELECT COUNT(e) FROM ExternalProductInsight e WHERE e.source = :source")
    long countBySource(@Param("source") String source);

    @Query("SELECT MAX(e.updatedAt) FROM ExternalProductInsight e")
    LocalDateTime findLastUpdatedAt();
}
