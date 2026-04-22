package com.ecommerce.service;

import com.ecommerce.dto.BundleDTO;
import com.ecommerce.dto.CreateBundleRequest;
import java.util.List;

public interface BundleService {
    List<BundleDTO> getAllBundles();
    BundleDTO getBundleById(Long id);
    BundleDTO createBundle(CreateBundleRequest request);
    BundleDTO updateBundle(Long id, CreateBundleRequest request);
    void deleteBundle(Long id);
}
