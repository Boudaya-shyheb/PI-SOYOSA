package com.englishway.course.util;

import com.englishway.course.enums.Role;
import com.englishway.course.exception.BadRequestException;

public class RequestContext {
    private final String userId;
    private final Role role;
    private final boolean authenticated;

    private RequestContext(String userId, Role role, boolean authenticated) {
        this.userId = userId;
        this.role = role;
        this.authenticated = authenticated;
    }

    public static RequestContext fromHeaders(String userIdHeader, String roleHeader) {
        String userId = userIdHeader == null || userIdHeader.isBlank() ? null : userIdHeader.trim();

        Role role = Role.USER;
        if (roleHeader != null && !roleHeader.isBlank()) {
            try {
                String normalized = roleHeader.trim().toUpperCase();
                if (normalized.startsWith("ROLE_")) {
                    normalized = normalized.substring(5);
                }
                if ("TEACHER".equals(normalized)) {
                    normalized = "TUTOR";
                }
                role = Role.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                System.out.println("[DEBUG] Invalid Role Header received: " + roleHeader + ", falling back to USER");
                role = Role.USER;
            }
        }

        // Let's be less strict: if we have a userId, we are at least "authenticated" 
        // as someone, and specific role checks will happen in AccessControlService.
        boolean authenticated = userId != null && !userId.isBlank();

        return new RequestContext(userId, role, authenticated);
    }

    public String getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
