package com.ecommerce.service;

import com.ecommerce.dto.CouponDTO;
import com.ecommerce.dto.CreateCouponRequest;

import java.util.List;

public interface CouponService {
    List<CouponDTO> getAllCoupons();
    CouponDTO getCouponById(Long id);
    CouponDTO createCoupon(CreateCouponRequest request);
    CouponDTO updateCoupon(Long id, CreateCouponRequest request);
    void deleteCoupon(Long id);
}
