package com.ecommerce.dto;

import com.ecommerce.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponApplyResponse {
    private String code;
    private Coupon.CouponType type;
    private BigDecimal discountAmount;
    private Boolean freeShipping;
}
