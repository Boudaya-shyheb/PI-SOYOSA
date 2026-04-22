package com.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.entity.OrderCourierAssignment;

@Repository
public interface OrderCourierAssignmentRepository extends JpaRepository<OrderCourierAssignment, Long> {
    Optional<OrderCourierAssignment> findByOrderId(Long orderId);
    void deleteByOrderId(Long orderId);
}
