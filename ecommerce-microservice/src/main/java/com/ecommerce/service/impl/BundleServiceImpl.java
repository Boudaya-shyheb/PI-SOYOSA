package com.ecommerce.service.impl;

import com.ecommerce.dto.BundleDTO;
import com.ecommerce.dto.CreateBundleItemRequest;
import com.ecommerce.dto.CreateBundleRequest;
import com.ecommerce.entity.Bundle;
import com.ecommerce.entity.BundleItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.EntityMapper;
import com.ecommerce.repository.BundleRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.BundleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BundleServiceImpl implements BundleService {
    
    private final BundleRepository bundleRepository;
    private final ProductRepository productRepository;
    private final EntityMapper entityMapper;
    
    @Override
    @Transactional(readOnly = true)
    public List<BundleDTO> getAllBundles() {
        log.debug("Fetching all bundles");
        return bundleRepository.findAll().stream()
            .map(this::applyBundleDiscountSnapshot)
            .map(entityMapper::toBundleDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public BundleDTO getBundleById(Long id) {
        log.debug("Fetching bundle with id: {}", id);
        return bundleRepository.findById(id)
            .map(this::applyBundleDiscountSnapshot)
            .map(entityMapper::toBundleDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Bundle", "id", id));
    }
    
    @Override
    public BundleDTO createBundle(CreateBundleRequest request) {
        log.debug("Creating bundle with name: {}", request.getName());
        
        if (bundleRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("Bundle with name '" + request.getName() + "' already exists");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Bundle must contain at least one item");
        }
        
        Bundle bundle = Bundle.builder()
            .name(request.getName())
            .description(request.getDescription())
            .imageUrl(request.getImageUrl())
            .price(request.getPrice())
            .status(request.getStatus())
            .build();
        
        applyBundleItems(bundle, request.getItems());
        
        Bundle saved = bundleRepository.save(bundle);
        log.info("Bundle created successfully with id: {}", saved.getId());
        return entityMapper.toBundleDTO(saved);
    }
    
    @Override
    public BundleDTO updateBundle(Long id, CreateBundleRequest request) {
        log.debug("Updating bundle with id: {}", id);
        
        Bundle bundle = bundleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bundle", "id", id));
        
        if (!bundle.getName().equalsIgnoreCase(request.getName()) &&
            bundleRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("Bundle with name '" + request.getName() + "' already exists");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Bundle must contain at least one item");
        }
        
        bundle.setName(request.getName());
        bundle.setDescription(request.getDescription());
        bundle.setImageUrl(request.getImageUrl());
        bundle.setPrice(request.getPrice());
        bundle.setStatus(request.getStatus());
        
        bundle.getItems().clear();
        applyBundleItems(bundle, request.getItems());
        
        Bundle updated = bundleRepository.save(bundle);
        log.info("Bundle updated successfully with id: {}", id);
        return entityMapper.toBundleDTO(updated);
    }
    
    @Override
    public void deleteBundle(Long id) {
        log.debug("Deleting bundle with id: {}", id);
        
        Bundle bundle = bundleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bundle", "id", id));
        
        bundleRepository.delete(bundle);
        log.info("Bundle deleted successfully with id: {}", id);
    }
    
    private void applyBundleItems(Bundle bundle, List<CreateBundleItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        for (CreateBundleItemRequest itemRequest : items) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));
            BundleItem item = BundleItem.builder()
                .product(product)
                .quantity(itemRequest.getQuantity())
                .build();
            bundle.addItem(item);
        }
    }

    private Bundle applyBundleDiscountSnapshot(Bundle bundle) {
        if (bundle.getItems() == null || bundle.getItems().isEmpty()) {
            return bundle;
        }
        for (BundleItem item : bundle.getItems()) {
            Product product = item.getProduct();
            if (product == null || product.getDiscountEndsAt() == null) {
                continue;
            }
            if (product.getDiscountEndsAt().isAfter(LocalDateTime.now())) {
                continue;
            }
            Product snapshot = Product.builder()
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .price(product.getOriginalPrice() != null ? product.getOriginalPrice() : product.getPrice())
                .originalPrice(null)
                .discountPercent(null)
                .discountEndsAt(null)
                .quantityAvailable(product.getQuantityAvailable())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .category(product.getCategory())
                .build();
            snapshot.setId(product.getId());
            snapshot.setCreatedAt(product.getCreatedAt());
            snapshot.setUpdatedAt(product.getUpdatedAt());
            item.setProduct(snapshot);
        }
        return bundle;
    }
}
