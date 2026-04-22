package com.englishway.course.service;

import com.englishway.course.enums.Role;
import com.englishway.course.exception.AccessDeniedException;
import com.englishway.course.util.RequestContext;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {
    public void requireAuthenticated(RequestContext context) {
        if (!context.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
    }

    public void requireStudent(RequestContext context) {
        requireAuthenticated(context);
        if (context.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Only STUDENT can enroll and access enrolled content");
        }
    }

    public void requireTutorOrAdmin(RequestContext context) {
        requireAuthenticated(context);
        if (context.getRole() != Role.TUTOR && context.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Tutor or Admin role required");
        }
    }

    public void requireContentWrite(Role role) {
        if (role != Role.ADMIN && role != Role.TUTOR && role != Role.TEACHER) {
            throw new AccessDeniedException("Only ADMIN or TUTOR can modify learning content");
        }
    }

    public void requireCourseOwnership(RequestContext context, String ownerUserId) {
        requireAuthenticated(context);
        if (context.getRole() == Role.ADMIN) {
            return;
        }
        if (context.getRole() != Role.TUTOR && context.getRole() != Role.TEACHER) {
            throw new AccessDeniedException("Only ADMIN or TUTOR can modify courses");
        }
        if (ownerUserId == null || !ownerUserId.equals(context.getUserId())) {
            throw new AccessDeniedException("You can modify only your own courses");
        }
    }
}
