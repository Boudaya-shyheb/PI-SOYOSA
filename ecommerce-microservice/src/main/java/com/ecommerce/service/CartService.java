package com.ecommerce.service;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CouponApplyResponse;

public interface CartService {
    CartDTO getCartByCustomerId(Long customerId);
    CartDTO addProductToCart(Long customerId, AddToCartRequest request);
    CartDTO addBundleToCart(Long customerId, Long bundleId, Integer quantity);
    CartDTO removeProductFromCart(Long customerId, Long productId);
    CartDTO removeBundleFromCart(Long customerId, Long bundleId);
    CartDTO updateCartItemQuantity(Long customerId, Long productId, Integer quantity);
    CartDTO updateBundleItemQuantity(Long customerId, Long bundleId, Integer quantity);
    CartDTO clearCart(Long customerId);
    CouponApplyResponse applyCoupon(Long customerId, String code);
}
