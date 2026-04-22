package com.ecommerce.mapper;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {
    
    public CategoryDTO toCategoryDTO(Category category) {
        return CategoryDTO.builder()
            .id(category.getId())
            .name(category.getName())
            .description(category.getDescription())
            .build();
    }
    
    public Category toCategory(CreateCategoryRequest request) {
        return Category.builder()
            .name(request.getName())
            .description(request.getDescription())
            .build();
    }
    
    public ProductDTO toProductDTO(Product product) {
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .isbn(product.getIsbn())
            .description(product.getDescription())
            .imageUrl(product.getImageUrl())
            .price(product.getPrice())
            .originalPrice(product.getOriginalPrice())
            .discountPercent(product.getDiscountPercent())
            .discountEndsAt(product.getDiscountEndsAt())
            .quantityAvailable(product.getQuantityAvailable())
            .categoryId(product.getCategory().getId())
            .categoryName(product.getCategory().getName())
            .averageRating(product.getAverageRating())
            .reviewCount(product.getReviewCount())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
    
    public Product toProduct(CreateProductRequest request) {
        return Product.builder()
            .name(request.getName())
            .isbn(request.getIsbn())
            .description(request.getDescription())
            .imageUrl(request.getImageUrl())
            .price(request.getPrice())
            .originalPrice(request.getOriginalPrice())
            .discountPercent(request.getDiscountPercent())
            .discountEndsAt(request.getDiscountEndsAt())
            .quantityAvailable(request.getQuantityAvailable())
            .build();
    }
    
    public OrderDTO toOrderDTO(Order order) {
        java.util.Set<OrderItem> items = order.getOrderItems() == null ? java.util.Collections.emptySet() : order.getOrderItems();
        return OrderDTO.builder()
            .id(order.getId())
            .customerId(order.getCustomerId())
            .status(order.getStatus().toString())
            .totalPrice(order.getTotalPrice())
            .deliveryStreet(order.getDeliveryStreet())
            .deliveryCity(order.getDeliveryCity())
            .deliveryPostalCode(order.getDeliveryPostalCode())
            .deliveryCountry(order.getDeliveryCountry())
            .deliveryLat(order.getDeliveryLat())
            .deliveryLng(order.getDeliveryLng())
            .orderItems(new java.util.HashSet<>(items).stream()
                .map(this::toOrderItemDTO)
                .collect(java.util.stream.Collectors.toSet()))
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
    
    public OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        return OrderItemDTO.builder()
            .id(orderItem.getId())
            .productId(orderItem.getProduct().getId())
            .productName(orderItem.getProduct().getName())
            .quantity(orderItem.getQuantity())
            .price(orderItem.getPrice())
            .subtotal(orderItem.getSubtotal())
            .createdAt(orderItem.getCreatedAt())
            .build();
    }
    
    public CartDTO toCartDTO(Cart cart) {
        java.util.Set<CartItem> items = cart.getCartItems() == null ? java.util.Collections.emptySet() : cart.getCartItems();
        return CartDTO.builder()
            .id(cart.getId())
            .customerId(cart.getCustomerId())
            .cartItems(new java.util.HashSet<>(items).stream()
                .map(this::toCartItemDTO)
                .collect(java.util.stream.Collectors.toSet()))
            .total(cart.getTotal())
            .totalItems(cart.getTotalItems())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }
    
    public CartItemDTO toCartItemDTO(CartItem cartItem) {
        CartItemDTO.CartItemDTOBuilder builder = CartItemDTO.builder()
            .id(cartItem.getId())
            .quantity(cartItem.getQuantity())
            .subtotal(cartItem.getSubtotal())
            .createdAt(cartItem.getCreatedAt());

        if (cartItem.getBundle() != null) {
            builder.itemType("BUNDLE")
                .bundleId(cartItem.getBundle().getId())
                .bundleName(cartItem.getBundle().getName())
                .bundlePrice(cartItem.getBundle().getPrice())
                .bundleImageUrl(cartItem.getBundle().getImageUrl());
        }

        if (cartItem.getProduct() != null) {
            builder.itemType("PRODUCT")
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .productPrice(cartItem.getProduct().getPrice())
                .productImageUrl(cartItem.getProduct().getImageUrl());
        }

        return builder.build();
    }

    public BundleDTO toBundleDTO(Bundle bundle) {
        java.util.Set<BundleItem> items = bundle.getItems() == null ? java.util.Collections.emptySet() : bundle.getItems();
        return BundleDTO.builder()
            .id(bundle.getId())
            .name(bundle.getName())
            .description(bundle.getDescription())
            .imageUrl(bundle.getImageUrl())
            .price(bundle.getPrice())
            .status(bundle.getStatus())
            .items(new java.util.HashSet<>(items).stream()
                .map(this::toBundleItemDTO)
                .collect(java.util.stream.Collectors.toSet()))
            .createdAt(bundle.getCreatedAt())
            .updatedAt(bundle.getUpdatedAt())
            .build();
    }

    public BundleItemDTO toBundleItemDTO(BundleItem item) {
        return BundleItemDTO.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productImageUrl(item.getProduct().getImageUrl())
            .productPrice(item.getProduct().getPrice())
            .categoryName(item.getProduct().getCategory().getName())
            .quantity(item.getQuantity())
            .build();
    }

    public CouponDTO toCouponDTO(Coupon coupon) {
        return CouponDTO.builder()
            .id(coupon.getId())
            .code(coupon.getCode())
            .description(coupon.getDescription())
            .type(coupon.getType())
            .value(coupon.getValue())
            .minOrderTotal(coupon.getMinOrderTotal())
            .maxDiscount(coupon.getMaxDiscount())
            .active(coupon.getActive())
            .usageLimit(coupon.getUsageLimit())
            .usedCount(coupon.getUsedCount())
            .startsAt(coupon.getStartsAt())
            .endsAt(coupon.getEndsAt())
            .createdAt(coupon.getCreatedAt())
            .updatedAt(coupon.getUpdatedAt())
            .build();
    }

    public Coupon toCoupon(CreateCouponRequest request) {
        return Coupon.builder()
            .code(request.getCode())
            .description(request.getDescription())
            .type(request.getType())
            .value(request.getValue())
            .minOrderTotal(request.getMinOrderTotal())
            .maxDiscount(request.getMaxDiscount())
            .active(request.getActive())
            .usageLimit(request.getUsageLimit())
            .startsAt(request.getStartsAt())
            .endsAt(request.getEndsAt())
            .build();
    }
}
