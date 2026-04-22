package com.ecommerce.service;

public interface SearchLogService {
    void logSearch(String keyword, String searchType, int resultCount, String authHeader);
}
