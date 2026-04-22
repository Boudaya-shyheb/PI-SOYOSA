package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carts")
@Data
@EqualsAndHashCode(exclude = "cartItems")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {
    
    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<CartItem> cartItems = new HashSet<>();
    
    public void addCartItem(CartItem item) {
        if (cartItems == null) {
            cartItems = new HashSet<>();
        }
        cartItems.add(item);
        item.setCart(this);
    }
    
    public void removeCartItem(CartItem item) {
        if (cartItems == null) {
            return;
        }
        cartItems.remove(item);
        item.setCart(null);
    }
    
    public void clearCart() {
        if (cartItems == null) {
            return;
        }
        cartItems.clear();
    }
    
    public BigDecimal getTotal() {
        if (cartItems == null) {
            return BigDecimal.ZERO;
        }
        return cartItems.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public Integer getTotalItems() {
        if (cartItems == null) {
            return 0;
        }
        return cartItems.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }
}
