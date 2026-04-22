package com.ecommerce.service;

import com.ecommerce.dto.AuditLogDTO;

import java.util.List;

public interface AuditLogService {
    void logAction(String authHeader, String action, String entityType, Long entityId, String description);
    List<AuditLogDTO> getRecent(int limit);
}
