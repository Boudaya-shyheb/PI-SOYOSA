package com.ecommerce.repository;

import com.ecommerce.entity.Bundle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BundleRepository extends JpaRepository<Bundle, Long> {
    boolean existsByNameIgnoreCase(String name);
}
