package com.ecommerce.dto;

import com.ecommerce.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDTO {
    private Long id;
    private String code;
    private String description;
    private Coupon.CouponType type;
    private BigDecimal value;
    private BigDecimal minOrderTotal;
    private BigDecimal maxDiscount;
    private Boolean active;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
