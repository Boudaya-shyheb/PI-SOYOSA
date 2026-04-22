package com.ecommerce.service.impl;

import com.ecommerce.entity.SearchLog;
import com.ecommerce.repository.SearchLogRepository;
import com.ecommerce.service.SearchLogService;
import com.ecommerce.service.UserServiceFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SearchLogServiceImpl implements SearchLogService {

    private final SearchLogRepository searchLogRepository;
    private final UserServiceFeignService userServiceFeignService;

    @Override
    public void logSearch(String keyword, String searchType, int resultCount, String authHeader) {
        String userId = null;
        try {
            if (authHeader != null && !authHeader.isBlank() && userServiceFeignService.validateToken(authHeader)) {
                userId = userServiceFeignService.getUserId(authHeader);
            }
        } catch (Exception e) {
            log.debug("Search log user resolution failed", e);
        }

        SearchLog logEntry = SearchLog.builder()
            .keyword(keyword != null ? keyword.trim() : "")
            .searchType(searchType)
            .resultCount(resultCount)
            .hasResults(resultCount > 0)
            .userId(userId)
            .build();

        searchLogRepository.save(logEntry);
    }
}
