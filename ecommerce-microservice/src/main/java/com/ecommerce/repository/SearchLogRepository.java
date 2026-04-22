package com.ecommerce.repository;

import com.ecommerce.dto.SearchKeywordCountDTO;
import com.ecommerce.entity.SearchLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    @Query("SELECT new com.ecommerce.dto.SearchKeywordCountDTO(s.keyword, COUNT(s)) " +
           "FROM SearchLog s " +
           "WHERE s.createdAt >= :since AND s.keyword IS NOT NULL AND s.keyword <> '' " +
           "GROUP BY s.keyword " +
           "ORDER BY COUNT(s) DESC")
    List<SearchKeywordCountDTO> findTopKeywordsSince(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT new com.ecommerce.dto.SearchKeywordCountDTO(s.keyword, COUNT(s)) " +
           "FROM SearchLog s " +
           "WHERE s.createdAt >= :since AND s.resultCount = 0 AND s.keyword IS NOT NULL AND s.keyword <> '' " +
           "GROUP BY s.keyword " +
           "ORDER BY COUNT(s) DESC")
    List<SearchKeywordCountDTO> findZeroResultKeywordsSince(@Param("since") LocalDateTime since, Pageable pageable);
}
