package com.ecommerce.controller;

import com.ecommerce.dto.ExternalInsightRecordDTO;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.service.AuditLogService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.SearchLogService;
import com.ecommerce.service.UserServiceFeignService;
import com.ecommerce.util.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private UserServiceFeignService userServiceFeignService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SearchLogService searchLogService;

    @Test
    void getInsightsCatalog_returnsPageWhenAdmin() {
        ProductController controller = new ProductController(
            productService,
            userServiceFeignService,
            auditLogService,
            searchLogService
        );

        PageResponse<ExternalInsightRecordDTO> page = PageResponse.<ExternalInsightRecordDTO>builder()
            .content(List.of(ExternalInsightRecordDTO.builder().id(1L).build()))
            .currentPage(0)
            .pageSize(25)
            .totalElements(1)
            .totalPages(1)
            .isFirst(true)
            .isLast(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();

        when(userServiceFeignService.validateToken("Bearer token")).thenReturn(true);
        when(userServiceFeignService.getUserRole("Bearer token")).thenReturn("ADMIN");
        when(productService.getExternalInsightsCatalog(0, 25)).thenReturn(page);

        ResponseEntity<ApiResponse<PageResponse<ExternalInsightRecordDTO>>> response =
            controller.getInsightsCatalog(0, 25, "Bearer token");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getContent()).hasSize(1);
        verify(productService).getExternalInsightsCatalog(0, 25);
    }

    @Test
    void getInsightsCatalog_throwsWhenNotAdmin() {
        ProductController controller = new ProductController(
            productService,
            userServiceFeignService,
            auditLogService,
            searchLogService
        );

        when(userServiceFeignService.validateToken("Bearer token")).thenReturn(true);
        when(userServiceFeignService.getUserRole("Bearer token")).thenReturn("CUSTOMER");

        assertThatThrownBy(() -> controller.getInsightsCatalog(0, 25, "Bearer token"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Admin privileges");
    }
}
