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
public class PageResponse<T> {

    private List<T> content;
    
    private int currentPage;
    
    private int pageSize;
    
    private long totalElements;
    
    private int totalPages;
    
    private boolean isFirst;
    
    private boolean isLast;
    
    private boolean hasNext;
    
    private boolean hasPrevious;
}
