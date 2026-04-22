package com.ecommerce.service.impl;

import com.ecommerce.dto.AuditLogDTO;
import com.ecommerce.entity.AuditLog;
import com.ecommerce.repository.AuditLogRepository;
import com.ecommerce.service.AuditLogService;
import com.ecommerce.service.UserServiceFeignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserServiceFeignService userServiceFeignService;

    @Override
    public void logAction(String authHeader, String action, String entityType, Long entityId, String description) {
        String actorId = null;
        String actorRole = null;
        try {
            if (authHeader != null && !authHeader.isBlank() && userServiceFeignService.validateToken(authHeader)) {
                actorId = userServiceFeignService.getUserId(authHeader);
                actorRole = userServiceFeignService.getUserRole(authHeader);
            }
        } catch (Exception e) {
            log.debug("Audit log actor resolution failed", e);
        }

        AuditLog logEntry = AuditLog.builder()
            .actorId(actorId)
            .actorRole(actorRole)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .description(description)
            .build();

        auditLogRepository.save(logEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getRecent(int limit) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).stream()
            .map(logEntry -> AuditLogDTO.builder()
                .id(logEntry.getId())
                .actorId(logEntry.getActorId())
                .actorRole(logEntry.getActorRole())
                .action(logEntry.getAction())
                .entityType(logEntry.getEntityType())
                .entityId(logEntry.getEntityId())
                .description(logEntry.getDescription())
                .createdAt(logEntry.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }
}
