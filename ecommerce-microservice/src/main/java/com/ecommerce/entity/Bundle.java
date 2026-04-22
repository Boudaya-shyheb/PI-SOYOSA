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
@Table(name = "bundles")
@Data
@EqualsAndHashCode(exclude = "items")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bundle extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.DRAFT;
    
    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<BundleItem> items = new HashSet<>();
    
    public void addItem(BundleItem item) {
        items.add(item);
        item.setBundle(this);
    }
    
    public void removeItem(BundleItem item) {
        items.remove(item);
        item.setBundle(null);
    }
    
    public enum Status {
        ACTIVE,
        DRAFT
    }
}
