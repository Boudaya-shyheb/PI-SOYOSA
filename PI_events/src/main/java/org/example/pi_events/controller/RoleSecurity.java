package org.example.pi_events.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

public final class RoleSecurity {

    private RoleSecurity() {
    }

    public static void requireAnyRole(String userRole, String... allowedRoles) {
        if (allowedRoles == null || allowedRoles.length == 0) {
            return;
        }
        String normalizedUserRole = normalizeRole(userRole);
        if (normalizedUserRole == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: role is required");
        }

        for (String allowedRole : allowedRoles) {
            String normalizedAllowedRole = normalizeRole(allowedRole);
            if (normalizedAllowedRole != null && normalizedAllowedRole.equals(normalizedUserRole)) {
                return;
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: insufficient role");
    }

    private static String normalizeRole(String role) {
        if (role == null) {
            return null;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return normalized;
    }
}
