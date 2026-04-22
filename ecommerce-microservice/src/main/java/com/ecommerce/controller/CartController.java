package com.ecommerce.controller;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CouponApplyResponse;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.CartService;
import com.ecommerce.service.UserServiceFeignService;
import com.ecommerce.util.ApiResponse;
import com.ecommerce.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ecommerce/carts")
@RequiredArgsConstructor
@Validated
@Slf4j
public class CartController {
    
    private final CartService cartService;
    private final UserServiceFeignService userServiceFeignService;
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(
        @PathVariable Long customerId,
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("GET /api/ecommerce/carts/customer/{} - Fetch cart", customerId);
        requireCustomerAccess(customerId, authHeader);
        CartDTO cart = cartService.getCartByCustomerId(customerId);
        return ResponseEntity.ok(new ApiResponse<>("Cart retrieved successfully", cart));
    }
    
    @PostMapping("/customer/{customerId}/add-item")
    public ResponseEntity<ApiResponse<CartDTO>> addToCart(
        @PathVariable Long customerId,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @Valid @RequestBody AddToCartRequest request) {
        log.info("POST /api/ecommerce/carts/customer/{}/add-item - Add product to cart", customerId);
        requireCustomerAccess(customerId, authHeader);
        CartDTO updated = cartService.addProductToCart(customerId, request);
        return ResponseEntity.status(HttpStatus.OK)
            .body(new ApiResponse<>("Product added to cart successfully", updated));
    }

    @PostMapping("/customer/{customerId}/add-bundle/{bundleId}")
    public ResponseEntity<ApiResponse<CartDTO>> addBundleToCart(
        @PathVariable Long customerId,
        @PathVariable Long bundleId,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestParam(defaultValue = "1") Integer quantity) {
        log.info("POST /api/ecommerce/carts/customer/{}/add-bundle/{} - Add bundle to cart", customerId, bundleId);
        requireCustomerAccess(customerId, authHeader);
        CartDTO updated = cartService.addBundleToCart(customerId, bundleId, quantity);
        return ResponseEntity.status(HttpStatus.OK)
            .body(new ApiResponse<>("Bundle added to cart successfully", updated));
    }
    
    @DeleteMapping("/customer/{customerId}/remove-item/{productId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeFromCart(
        @PathVariable Long customerId,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @PathVariable Long productId) {
        log.info("DELETE /api/ecommerce/carts/customer/{}/remove-item/{} - Remove product from cart", customerId, productId);
        requireCustomerAccess(customerId, authHeader);
        CartDTO updated = cartService.removeProductFromCart(customerId, productId);
        return ResponseEntity.ok(new ApiResponse<>("Product removed from cart successfully", updated));
    }

    @DeleteMapping("/customer/{customerId}/remove-bundle/{bundleId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeBundleFromCart(
        @PathVariable Long customerId,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @PathVariable Long bundleId) {
        log.info("DELETE /api/ecommerce/carts/customer/{}/remove-bundle/{} - Remove bundle from cart", customerId, bundleId);
        requireCustomerAccess(customerId, authHeader);
        CartDTO updated = cartService.removeBundleFromCart(customerId, bundleId);
        return ResponseEntity.ok(new ApiResponse<>("Bundle removed from cart successfully", updated));
    }
    
    @PutMapping("/customer/{customerId}/update-item/{productId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItemQuantity(
        @PathVariable Long customerId,
        @PathVariable Long productId,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestParam Integer quantity) {
        log.info("PUT /api/ecommerce/carts/customer/{}/update-item/{} - Update product quantity to {}", 
            customerId, productId, quantity);
        requireCustomerAccess(customerId, authHeader);
        CartDTO updated = cartService.updateCartItemQuantity(customerId, productId, quantity);
        return ResponseEntity.ok(new ApiResponse<>("Cart item quantity updated successfully", updated));
    }

    @PutMapping("/customer/{customerId}/update-bundle/{bundleId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateBundleItemQuantity(
        @PathVariable Long customerId,
        @PathVariable Long bundleId,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestParam Integer quantity) {
        log.info("PUT /api/ecommerce/carts/customer/{}/update-bundle/{} - Update bundle quantity to {}", 
            customerId, bundleId, quantity);
        requireCustomerAccess(customerId, authHeader);
        CartDTO updated = cartService.updateBundleItemQuantity(customerId, bundleId, quantity);
        return ResponseEntity.ok(new ApiResponse<>("Bundle quantity updated successfully", updated));
    }
    
    @DeleteMapping("/customer/{customerId}/clear")
    public ResponseEntity<ApiResponse<CartDTO>> clearCart(
        @PathVariable Long customerId,
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("DELETE /api/ecommerce/carts/customer/{}/clear - Clear cart", customerId);
        requireCustomerAccess(customerId, authHeader);
        CartDTO cleared = cartService.clearCart(customerId);
        return ResponseEntity.ok(new ApiResponse<>("Cart cleared successfully", cleared));
    }

    @PostMapping("/customer/{customerId}/apply-coupon")
    public ResponseEntity<ApiResponse<CouponApplyResponse>> applyCoupon(
        @PathVariable Long customerId,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestParam String code) {
        log.info("POST /api/ecommerce/carts/customer/{}/apply-coupon - Apply coupon", customerId);
        requireCustomerAccess(customerId, authHeader);
        CouponApplyResponse response = cartService.applyCoupon(customerId, code);
        return ResponseEntity.ok(new ApiResponse<>("Coupon applied successfully", response));
    }

    private void requireCustomerAccess(Long customerId, String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new BusinessException("Authorization header required");
        }
        if (!userServiceFeignService.validateToken(authHeader)) {
            throw new BusinessException("Invalid or expired token");
        }
        String userKey = userServiceFeignService.getUserId(authHeader);
        if (userKey == null || userKey.isBlank()) {
            throw new BusinessException("Invalid user identity");
        }
        UserServiceClient.UserInfoDto info = userServiceFeignService.getUserInfo(userKey);
        String resolvedUserId = info != null ? info.getUserId() : null;
        if (resolvedUserId == null || resolvedUserId.isBlank()) {
            throw new BusinessException("Invalid user identity");
        }
        try {
            Long tokenCustomerId = Long.valueOf(resolvedUserId);
            if (!tokenCustomerId.equals(customerId)) {
                throw new BusinessException("Customer does not match token");
            }
        } catch (NumberFormatException e) {
            throw new BusinessException("Invalid customer identity");
        }
    }
}
