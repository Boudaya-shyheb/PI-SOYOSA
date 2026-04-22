package com.ecommerce.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_courier_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCourierAssignment extends BaseEntity {

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "courier_id", nullable = false)
    private Long courierId;

    @Column(name = "courier_name")
    private String courierName;

    @Column(name = "assigned_by")
    private Long assignedBy;
}
