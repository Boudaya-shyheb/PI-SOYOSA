package com.ecommerce.dto;

import com.ecommerce.entity.Coupon;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code cannot be blank")
    @Size(min = 2, max = 50, message = "Coupon code must be between 2 and 50 characters")
    private String code;

    private String description;

    @NotNull(message = "Coupon type is required")
    private Coupon.CouponType type;

    @NotNull(message = "Coupon value is required")
    @DecimalMin(value = "0.01", message = "Coupon value must be greater than 0")
    private BigDecimal value;

    @DecimalMin(value = "0.01", message = "Minimum order total must be greater than 0")
    private BigDecimal minOrderTotal;

    @DecimalMin(value = "0.01", message = "Maximum discount must be greater than 0")
    private BigDecimal maxDiscount;

    @NotNull(message = "Active status is required")
    private Boolean active;

    private Integer usageLimit;

    private LocalDateTime startsAt;

    private LocalDateTime endsAt;
}
