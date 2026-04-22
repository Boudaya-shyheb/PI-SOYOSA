package com.ecommerce.tracking.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.ecommerce.tracking.dto.LocationUpdate;

@Service
public class LocationStore {
    private final ConcurrentHashMap<Long, LocationUpdate> latest = new ConcurrentHashMap<>();

    public void saveLatest(Long orderId, LocationUpdate dto) {
        latest.put(orderId, dto);
    }

    public LocationUpdate getLatest(Long orderId) {
        return latest.get(orderId);
    }
}
