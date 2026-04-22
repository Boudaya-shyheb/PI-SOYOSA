package com.ecommerce.service.impl;

import com.ecommerce.dto.ExternalInsightRecordDTO;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.ExternalProductInsight;
import com.ecommerce.entity.Product;
import com.ecommerce.mapper.EntityMapper;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ExternalProductInsightRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplExternalInsightsCatalogTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExternalProductInsightRepository externalProductInsightRepository;

    @Mock
    private EntityMapper entityMapper;

    @Test
    void getExternalInsightsCatalog_marksMatchedWhenIsbnMatches() {
        Product product = new Product();
        product.setId(10L);
        product.setName("Learning Spring");
        product.setIsbn("978-1-4028-9462-6");
        Category category = new Category();
        category.setName("Books");
        product.setCategory(category);

        ExternalProductInsight insight = new ExternalProductInsight();
        insight.setId(77L);
        insight.setIsbn("9781402894626");
        insight.setTitle("Learning Spring");
        insight.setProduct(product);

        when(externalProductInsightRepository.findAllWithProduct(any()))
            .thenReturn(new PageImpl<>(List.of(insight), PageRequest.of(0, 25), 1));

        ProductServiceImpl service = new ProductServiceImpl(
            productRepository,
            categoryRepository,
            externalProductInsightRepository,
            entityMapper
        );

        PageResponse<ExternalInsightRecordDTO> response = service.getExternalInsightsCatalog(0, 25);

        assertThat(response.getContent()).hasSize(1);
        ExternalInsightRecordDTO record = response.getContent().get(0);
        assertThat(record.isMatched()).isTrue();
        assertThat(record.getProductId()).isEqualTo(10L);
    }

    @Test
    void getExternalInsightsCatalog_marksUnmatchedWhenTitleIsDifferentForNonBooks() {
        Product product = new Product();
        product.setId(11L);
        product.setName("Wireless Mouse Pro");
        Category category = new Category();
        category.setName("Electronics");
        product.setCategory(category);

        ExternalProductInsight insight = new ExternalProductInsight();
        insight.setId(78L);
        insight.setTitle("Cooking Basics");
        insight.setProduct(product);

        when(externalProductInsightRepository.findAllWithProduct(any()))
            .thenReturn(new PageImpl<>(List.of(insight), PageRequest.of(0, 25), 1));

        ProductServiceImpl service = new ProductServiceImpl(
            productRepository,
            categoryRepository,
            externalProductInsightRepository,
            entityMapper
        );

        PageResponse<ExternalInsightRecordDTO> response = service.getExternalInsightsCatalog(0, 25);

        assertThat(response.getContent()).hasSize(1);
        ExternalInsightRecordDTO record = response.getContent().get(0);
        assertThat(record.isMatched()).isFalse();
    }
}
