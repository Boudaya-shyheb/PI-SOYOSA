package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatedProductsDTO {

    private List<ProductDTO> relatedProducts;
    
    private List<ProductDTO> bestSellers;
    
    private List<ProductDTO> trending;
    
    private List<ProductDTO> recentlyViewed;
}
