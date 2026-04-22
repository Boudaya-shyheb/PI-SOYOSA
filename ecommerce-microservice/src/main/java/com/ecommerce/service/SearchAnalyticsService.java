package com.ecommerce.service;

import com.ecommerce.dto.SearchKeywordCountDTO;

import java.util.List;

public interface SearchAnalyticsService {
    List<SearchKeywordCountDTO> getTopSearches(int days, int limit);
    List<SearchKeywordCountDTO> getZeroResultSearches(int days, int limit);
}
