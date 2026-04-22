package com.ecommerce.service.impl;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CouponApplyResponse;
import com.ecommerce.entity.Bundle;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Coupon;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.EntityMapper;
import com.ecommerce.repository.BundleRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CouponRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final BundleRepository bundleRepository;
    private final CouponRepository couponRepository;
    private final EntityMapper entityMapper;

    @Override
    public CartDTO getCartByCustomerId(Long customerId) {
        log.debug("Fetching cart for customer: {}", customerId);

        Cart cart = cartRepository.findByCustomerId(customerId).orElse(null);
        if (cart == null) {
            return CartDTO.builder()
                    .customerId(customerId)
                    .cartItems(new HashSet<>())
                    .total(BigDecimal.ZERO)
                    .totalItems(0)
                    .build();
        }

        return entityMapper.toCartDTO(cart);
    }

    @Override
    @Transactional(readOnly = false)
    public CartDTO addProductToCart(Long customerId, AddToCartRequest request) {
        log.debug("Adding product {} to cart for customer {}", request.getProductId(), customerId);

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createNewCart(customerId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        normalizeDiscount(product);

        // Check stock
        if (product.getQuantityAvailable() < request.getQuantity()) {
            throw new BusinessException("Insufficient stock for product: " + product.getName() +
                    ". Available: " + product.getQuantityAvailable());
        }

        // Check if product already in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId())
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getQuantityAvailable() < newQuantity) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.saveAndFlush(existingItem);
            log.info("Updated quantity of product {} in cart to {}", request.getProductId(), newQuantity);
        } else {
            // Add new item - explicitly save CartItem with flush to ensure immediate
            // persistence
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.saveAndFlush(cartItem); // Flush immediately
            cart.addCartItem(cartItem);
            cartRepository.saveAndFlush(cart); // Flush cart changes
            log.info("Added product {} to cart for customer {}", request.getProductId(), customerId);
        }

        // Reload cart from database to ensure we have the latest state including all
        // items
        cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        return entityMapper.toCartDTO(cart);
    }

    @Override
    @Transactional(readOnly = false)
    public CartDTO addBundleToCart(Long customerId, Long bundleId, Integer quantity) {
        log.debug("Adding bundle {} to cart for customer {}", bundleId, customerId);

        int safeQuantity = quantity == null ? 1 : quantity;
        if (safeQuantity <= 0) {
            throw new BusinessException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createNewCart(customerId));

        Bundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle", "id", bundleId));

        CartItem existingItem = cartItemRepository.findByCartIdAndBundleId(cart.getId(), bundleId)
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + safeQuantity;
            existingItem.setQuantity(newQuantity);
            cartItemRepository.saveAndFlush(existingItem);
            log.info("Updated quantity of bundle {} in cart to {}", bundleId, newQuantity);
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .bundle(bundle)
                    .quantity(safeQuantity)
                    .build();
            cartItemRepository.saveAndFlush(cartItem);
            cart.addCartItem(cartItem);
            cartRepository.saveAndFlush(cart);
            log.info("Added bundle {} to cart for customer {}", bundleId, customerId);
        }

        cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        return entityMapper.toCartDTO(cart);
    }

    @Override
    @Transactional(readOnly = false)
    public CartDTO removeProductFromCart(Long customerId, Long productId) {
        log.debug("Removing product {} from cart for customer {}", productId, customerId);

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId + " in cart"));

        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);
        cartItemRepository.flush();
        cartRepository.saveAndFlush(cart);
        log.info("Removed product {} from cart for customer {}", productId, customerId);

        // Reload cart to ensure latest state
        cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        return entityMapper.toCartDTO(cart);
    }

    @Override
    @Transactional(readOnly = false)
    public CartDTO removeBundleFromCart(Long customerId, Long bundleId) {
        log.debug("Removing bundle {} from cart for customer {}", bundleId, customerId);

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        CartItem cartItem = cartItemRepository.findByCartIdAndBundleId(cart.getId(), bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle", "id", bundleId + " in cart"));

        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);
        cartItemRepository.flush();
        cartRepository.saveAndFlush(cart);
        log.info("Removed bundle {} from cart for customer {}", bundleId, customerId);

        cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        return entityMapper.toCartDTO(cart);
    }

    @Override
    @Transactional(readOnly = false)
    public CartDTO updateBundleItemQuantity(Long customerId, Long bundleId, Integer quantity) {
        log.debug("Updating quantity of bundle {} in cart for customer {}", bundleId, customerId);

        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        CartItem cartItem = cartItemRepository.findByCartIdAndBundleId(cart.getId(), bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle", "id", bundleId + " in cart"));

        cartItem.setQuantity(quantity);
        cartItemRepository.saveAndFlush(cartItem);
        cartRepository.saveAndFlush(cart);
        log.info("Updated quantity of bundle {} to {}", bundleId, quantity);

        cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        return entityMapper.toCartDTO(cart);
    }

    @Override
    @Transactional(readOnly = false)
    public CartDTO updateCartItemQuantity(Long customerId, Long productId, Integer quantity) {
        log.debug("Updating quantity of product {} in cart for customer {}", productId, customerId);

        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId + " in cart"));

        Product product = cartItem.getProduct();
        normalizeDiscount(product);
        if (product.getQuantityAvailable() < quantity) {
            throw new BusinessException("Insufficient stock for product: " + product.getName() +
                    ". Available: " + product.getQuantityAvailable());
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.saveAndFlush(cartItem);
        cartRepository.saveAndFlush(cart);
        log.info("Updated quantity of product {} to {}", productId, quantity);

        // Reload cart to ensure latest state
        cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        return entityMapper.toCartDTO(cart);
    }

    @Transactional(readOnly = false)
    @Override
    public CartDTO clearCart(Long customerId) {
        log.debug("Clearing cart for customer: {}", customerId);

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        cart.clearCart();
        cartRepository.saveAndFlush(cart);
        log.info("Cart cleared for customer: {}", customerId);

        // Reload cart to ensure latest state
        cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("Cart not found for customer: " + customerId));

        return entityMapper.toCartDTO(cart);
    }

    @Override
    @Transactional
    public CouponApplyResponse applyCoupon(Long customerId, String code) {
        log.debug("Applying coupon '{}' for customer {}", code, customerId);

        // Load cart
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException("No cart found for customer: " + customerId));

        // Calculate cart total from current items
        java.math.BigDecimal cartTotal = cart.getCartItems().stream()
                .map(item -> item.getSubtotal() != null ? item.getSubtotal() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Look up coupon
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException("Coupon '" + code + "' not found"));

        // Validate: active
        if (!Boolean.TRUE.equals(coupon.getActive())) {
            throw new BusinessException("Coupon '" + code + "' is no longer active");
        }

        // Validate: date range
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            throw new BusinessException("Coupon '" + code + "' is not yet valid");
        }
        if (coupon.getEndsAt() != null && now.isAfter(coupon.getEndsAt())) {
            throw new BusinessException("Coupon '" + code + "' has expired");
        }

        // Validate: usage limit
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BusinessException("Coupon '" + code + "' usage limit has been reached");
        }

        // Validate: minimum order total
        if (coupon.getMinOrderTotal() != null
                && cartTotal.compareTo(coupon.getMinOrderTotal()) < 0) {
            throw new BusinessException(
                    "Minimum order total of " + coupon.getMinOrderTotal() + " TND required for this coupon");
        }

        // Calculate discount
        java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;
        boolean freeShipping = false;

        switch (coupon.getType()) {
            case PERCENT -> {
                discountAmount = cartTotal
                        .multiply(coupon.getValue())
                        .divide(new java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                if (coupon.getMaxDiscount() != null
                        && discountAmount.compareTo(coupon.getMaxDiscount()) > 0) {
                    discountAmount = coupon.getMaxDiscount();
                }
            }
            case FIXED -> {
                discountAmount = coupon.getValue().min(cartTotal);
            }
            case FREE_SHIPPING -> {
                freeShipping = true;
                discountAmount = java.math.BigDecimal.ZERO;
            }
        }

        log.info("Coupon '{}' validated for customer {}: discount={}, freeShipping={}",
            code, customerId, discountAmount, freeShipping);

        return CouponApplyResponse.builder()
                .code(coupon.getCode())
                .type(coupon.getType())
                .discountAmount(discountAmount)
                .freeShipping(freeShipping)
                .build();
    }

    private Cart createNewCart(Long customerId) {
        log.debug("Creating new cart for customer: {}", customerId);
        Cart cart = Cart.builder()
                .customerId(customerId)
                .cartItems(new java.util.HashSet<>())
                .build();
        return cartRepository.save(cart);
    }

    private void normalizeDiscount(Product product) {
        if (product == null || product.getDiscountEndsAt() == null) {
            return;
        }
        if (product.getDiscountEndsAt().isAfter(LocalDateTime.now())) {
            return;
        }
        if (product.getOriginalPrice() != null) {
            product.setPrice(product.getOriginalPrice());
        }
        product.setOriginalPrice(null);
        product.setDiscountPercent(null);
        product.setDiscountEndsAt(null);
        productRepository.save(product);
    }
}
