package com.ecommerce.service.impl;

import com.ecommerce.dto.CouponDTO;
import com.ecommerce.dto.CreateCouponRequest;
import com.ecommerce.entity.Coupon;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.EntityMapper;
import com.ecommerce.repository.CouponRepository;
import com.ecommerce.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final EntityMapper entityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CouponDTO> getAllCoupons() {
        log.debug("Fetching all coupons");
        return couponRepository.findAll().stream()
            .map(entityMapper::toCouponDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CouponDTO getCouponById(Long id) {
        log.debug("Fetching coupon with id: {}", id);
        return couponRepository.findById(id)
            .map(entityMapper::toCouponDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
    }

    @Override
    public CouponDTO createCoupon(CreateCouponRequest request) {
        log.debug("Creating coupon with code: {}", request.getCode());

        if (couponRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new BusinessException("Coupon with code '" + request.getCode() + "' already exists");
        }

        Coupon coupon = entityMapper.toCoupon(request);
        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon created successfully with id: {}", saved.getId());
        return entityMapper.toCouponDTO(saved);
    }

    @Override
    public CouponDTO updateCoupon(Long id, CreateCouponRequest request) {
        log.debug("Updating coupon with id: {}", id);

        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));

        if (!coupon.getCode().equalsIgnoreCase(request.getCode()) &&
            couponRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new BusinessException("Coupon with code '" + request.getCode() + "' already exists");
        }

        coupon.setCode(request.getCode());
        coupon.setDescription(request.getDescription());
        coupon.setType(request.getType());
        coupon.setValue(request.getValue());
        coupon.setMinOrderTotal(request.getMinOrderTotal());
        coupon.setMaxDiscount(request.getMaxDiscount());
        coupon.setActive(request.getActive());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setStartsAt(request.getStartsAt());
        coupon.setEndsAt(request.getEndsAt());

        Coupon updated = couponRepository.save(coupon);
        log.info("Coupon updated successfully with id: {}", id);
        return entityMapper.toCouponDTO(updated);
    }

    @Override
    public void deleteCoupon(Long id) {
        log.debug("Deleting coupon with id: {}", id);
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        couponRepository.delete(coupon);
        log.info("Coupon deleted successfully with id: {}", id);
    }
}
