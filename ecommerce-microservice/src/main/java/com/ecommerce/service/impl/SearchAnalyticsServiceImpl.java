package com.ecommerce.service.impl;

import com.ecommerce.dto.SearchKeywordCountDTO;
import com.ecommerce.repository.SearchLogRepository;
import com.ecommerce.service.SearchAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchAnalyticsServiceImpl implements SearchAnalyticsService {

    private final SearchLogRepository searchLogRepository;

    @Override
    public List<SearchKeywordCountDTO> getTopSearches(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return searchLogRepository.findTopKeywordsSince(since, PageRequest.of(0, limit));
    }

    @Override
    public List<SearchKeywordCountDTO> getZeroResultSearches(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return searchLogRepository.findZeroResultKeywordsSince(since, PageRequest.of(0, limit));
    }
}
